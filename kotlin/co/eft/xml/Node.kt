package co.eft.xml

import java.util.NoSuchElementException


sealed class Node(open val parent: Node?, open val name: String, open val value: String) {

    val isRoot: Boolean
        get() = parent == null

    open val textValue: String
        get() = "<undefined textValue>"

    class Doc(name: String) : Node(parent=null, name="<<Doc_URI=$name>>", value="<<The_Doc_Itself>>") {

        private var _docElement: Elem? = null

        fun setDocElement(elem: Elem) {
            require (_docElement == null) { "Should only be invoked on Doc under construction!" }
            _docElement = elem
        }

        val docElement: Elem
            get() = _docElement!!
    }

    class Elem(override val parent: Node, name: String) : Node(parent, name, value="$name") {
        val attribs: Map<String, Attr> = LinkedHashMap<String, Attr>()
        val children: MutableList<Node> = mutableListOf<Node>()

        infix fun setAttribs(group: Collection<Attr>) {
            require (attribs.isEmpty()) {
                throw IllegalStateException("Should only be invoked on Elem under construction!")
            }

            val mm = attribs as MutableMap
            try {
                group.forEach {
                    require (!mm.containsKey(it.name)) { "Duplicate attribute: '${it.name}'" }
                    require (it.parent == this) { "Should only be invoked with set of own children Attr(s)!" }
                    mm[it.name] = it
                }
            }
            catch (exn: Exception) {
                attribs.clear()
                throw exn
            }
        }

        infix fun setChildren(group: Collection<Node>) {
            require (children.isEmpty()) {
                throw IllegalStateException("Should only be invoked on Elem under construction!")
            }
            require (group.all { it.parent == this }) {
                "Should only be invoked with set of own children Node(s)!"
            }

            children.addAll(group)
        }

        infix fun attr(attr_name: String): String = attribs[attr_name]!!.value

        // Provide for attr access like this: elem["attr_name"]
        operator fun get(attr_name: String): String? = attribs[attr_name]?.value

        operator fun set(attr_name: String, attr_value: String): String {
            (attribs as MutableMap)[attr_name] = Attr(this, attr_name, attr_value)
            return attr_value
        }

        private fun nullElem(elemName: String) = Elem(parent=this, name="<<null $elemName>>")
        private fun nullText() = Text(parent=this, value="<<null #text>>")
        private fun isNullElem() = name.startsWith("<<null ") && name.endsWith(">>")

        operator fun  div(xpath: String) = evalXpath(xpath.split('/'))

        fun  evalXpath(xpath: List<String>, cur: Int=0): Node {
            if (cur >= xpath.size)
                return this

            val elemName = xpath[cur]
            val optional = elemName.startsWith('(') && elemName.endsWith(")?")
            val actualName = (if (optional)
                                 elemName.substring(1, elemName.length - 2)
                              else
                                 elemName)

            if (actualName == "#") {
                require(cur + 1 == xpath.size) { "/# must be the last component of a xpath but is not: $xpath" }
                if (isNullElem())
                    return nullText()

                with (children("#")) {
                    if (any())
                        return single() as Text
                    else if (optional)
                        return nullText()
                    else
                        throw NoSuchElementException("No #text child element in xpath: ${xpath.render()}")
                }
            }
            else { // not a #text
                with(children(actualName)) {
                    if (any())
                        return (single() as Elem).evalXpath(xpath, cur + 1)
                    else if (optional)
                        return nullElem(actualName)
                    else
                        throw NoSuchElementException("No '$actualName' child element in xpath: ${xpath.render()}")
                }
            }
        }

        operator fun  rem(childElementName: String): Elem? =
            with (children(childElementName)) {
                return if (any())
                    single() as Elem
                else
                    null
            }

        operator fun  times(childElementName: String): Sequence<Elem> = children(childElementName) as Sequence<Elem>

        operator fun  div(ch: Char): String {
            require(ch == '#') { "Only '#' is supported !" }
            if (isNullElem())
                return "<undefined textValue>"
            else
                return (children("#text").single() as Text).value
        }
        operator fun  times(ch: Char): Sequence<Text> {
            require(ch == '#') { "Only '#' is supported !" }
            return children("#text") as Sequence<Text>
        }

        fun allChildren(): Sequence<Node> = children.asSequence()

        fun elementsChildren(): Sequence<Elem> = children.asSequence().filter { it is Elem } as Sequence<Elem>

        fun children(child_name: String): Sequence<Node> =
            children.asSequence().filter { elem -> elem.name == child_name }

        fun children(child_name_re: Regex): Sequence<Node> =
            children.asSequence().filter { elem -> child_name_re.matches(elem.name) }

        override fun toString() = "<$name ..."

        override val textValue: String
            get() = allChildren().joinToString(separator="\n") { it.textValue }

    }

    class Attr(parent: Node, name: String, value: String) : Node(parent, name, value) {
        override fun toString() = "$name=$value"

        override val textValue: String
            get() = value
    }

    class Text(parent: Node, value: String) : Node(parent, name="#", value=value) {
        override fun toString() = "#text:'$value'"

        override val textValue: String
            get() = value
    }
}


private fun <T> List<T>.render() = joinToString("/")