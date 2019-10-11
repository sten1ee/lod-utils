package co.eft.xml

import co.eft.util.filter
//import java.lang.Exception
//import java.lang.IllegalStateException

sealed class Node(open val parent: Node?, internal val name: String) {

    val isRoot: Boolean
        get() = parent == null

    class Doc(name: String) : Node(null, name) {

        var element: Elem? = null
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

        infix fun attr(attr_name: String): String? = attribs[attr_name]?.value

        // Provide for attr access like this: elem["attr_name"]
        fun get(attr_name: String): String? = attribs[attr_name]?.value

        fun set(attr_name: String, attr_value: String): String {
            (attribs as MutableMap)[attr_name] = Attr(this, attr_name, attr_value)
            return attr_value
        }

        fun allChildren(): Iterable<Node> = Iterable() {
            -> children.iterator()
        }

        fun children(child_name: String): Iterable<Node> = Iterable() {
            -> children.iterator().filter { elem -> elem.name == child_name }
        }

        fun children(child_name_re: Regex): Iterable<Node> = Iterable() {
            -> children.iterator().filter { elem -> child_name_re.matches(elem.name) }
        }
    }

    class Attr(parent: Node, name: String, val value: String) : Node(parent, name) {
        override fun toString(): String = "$name=$value"
    }

    class Text(parent: Node, val value: String) : Node(parent, "#text") {
        override fun toString(): String = "#text:$value"
    }
}
