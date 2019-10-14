package co.eft.xml


fun main(args: Array<String>) {
    val re = Regex("[^\\p{L}\\d\\s.,‘’()/-]")

    val doc = DocBuilder().build("contributors/contributors.xml")

    doc.allTextNodesThat { re.containsMatchIn(it) }
       .forEachIndexed { i, node -> println("$i: $node") }

    var i = 0
    doc.forEachElem { elem ->
        if (elem.name == "person") {
            elem.let { person ->
                val id = person % "xml:id"
                val name = person/"label"/'#'
                println("""person[${i++}]: id=${id}, name='$name'""")
            }
        }
    }
}

fun  Node.allTextNodesThat(predicate: (String) -> Boolean): List<Node.Text> =
    allTextNodesThat(predicate, mutableListOf())

fun  Node.allTextNodesThat(predicate: (String) -> Boolean, result: MutableList<Node.Text>): List<Node.Text> {
    when (this) {
        is Node.Text-> if (predicate(value)) {
                           result.add(this)
                       }
        is Node.Doc -> docElement?.let {
                           it.allTextNodesThat(predicate, result)
                       }
        is Node.Elem-> allChildren().forEach {
                           child -> child.allTextNodesThat(predicate, result)
                       }
        else -> throw throw IllegalArgumentException("Unexpected argument type: ${this::class}")
    }
    return result
}


fun  Node.forEachNode(consume: (Node) -> Unit) {
    if (this is Node.Doc)
        docElement?.forEachNode(consume)
    else {
        consume(this)
        if (this is Node.Elem) {
            attribs.values.forEach { it.forEachNode(consume) }
            children.forEach { it.forEachNode(consume) }
        }
    }
}

fun  Node.forEachElem(consume: (Node.Elem) -> Unit) {
    if (this is Node.Doc)
        docElement?.forEachElem(consume)
    else if (this is Node.Elem) {
        consume(this)
        children.forEach { it.forEachElem(consume) }
    }
}

/*
fun  Node.forEach(consumer: Consumer<Node>) {
    when (this) {
        is Node.Doc ->
            docElement?.forEach(consumer)

        is Node.Elem -> {
            attribs.values.forEach { it.forEach(consumer) }
            children.forEach { it.forEach(consumer) }
        }
    }
}
*/