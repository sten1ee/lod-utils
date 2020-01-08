package co.eft.xml

import java.util.NoSuchElementException


sealed class Node(open val parent: Node?, internal val name: String) {

    val isRoot: Boolean
        get() = parent == null

    open val textValue: String
        get() = "<undefined textValue>"

    class Doc(name: String) : Node(null, name) {

        private var _docElement: Elem? = null

        fun setDocElement(elem: Elem) {
            require (_docElement == null) { "Should only be invoked on Doc under construction!" }
            _docElement = elem
        }

        val docElement: Elem
            get() = _docElement!!
    }

    class Elem(override val parent: Node, name: String) : Node(parent, name) {
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

        private fun nullElem(elemName: String) = Elem(this, "<null $elemName>")
        private fun isNullElem() = name.startsWith("<null ") && name.endsWith('>')

        operator fun  div(elementName: String): Elem {
            val optional = elementName.startsWith('(') && elementName.endsWith(")?")
            val actualName = (if (optional)
                                 elementName.substring(1, elementName.length - 2)
                              else
                                 elementName)
            with (children(actualName)) {
                if (any())
                    return single() as Elem
                else if (optional)
                    return nullElem(actualName)
                else
                    throw NoSuchElementException("No '$actualName' child element.")
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

    class Attr(parent: Node, name: String, val value: String) : Node(parent, name) {
        override fun toString() = "$name=$value"

        override val textValue: String
            get() = value
    }

    class Text(parent: Node, val value: String) : Node(parent, "#text") {
        override fun toString() = "#text:'$value'"

        override val textValue: String
            get() = value
    }
}
