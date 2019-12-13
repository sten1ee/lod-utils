package co.eft.xml


fun main(args: Array<String>) {
    val re = Regex("[^\\p{L}\\d\\s.,‘’()/-]")

    val doc = DocBuilder().build("contributors/contributors.xml")

    doc.allTextNodesThat { re.containsMatchIn(it) }
       .forEachIndexed { i, node -> println("$i: $node") }

    var line = 0
    doc.nodes()
        .filter { it !is Node.Text || it.value != "" }
        .forEach { println("${++line}: $it") }

    var i = 0
    doc.elements()
       .filter { it.name == "person" }
       .forEach {
           val id = it["xml:id"]
           val name = it/"label"/'#'
           println("""person[${i++}]: id=$id, name='$name""")
       }
}

fun  Node.allTextNodesThat(predicate: (String) -> Boolean): List<Node.Text> =
    allTextNodesThat(predicate, mutableListOf())

fun  Node.allTextNodesThat(predicate: (String) -> Boolean, result: MutableList<Node.Text>): List<Node.Text> {
    when (this) {
        is Node.Text-> if (predicate(value)) {
                           result.add(this)
                       }
        is Node.Doc -> docElement.allTextNodesThat(predicate, result)

        is Node.Elem-> allChildren().forEach {
                           child -> child.allTextNodesThat(predicate, result)
                       }
        else -> throw throw IllegalArgumentException("Unexpected argument type: ${this::class}")
    }
    return result
}


fun  Node.forEachNode(consume: (Node) -> Unit) {
    if (this is Node.Doc)
        docElement.forEachNode(consume)
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
        docElement.forEachElem(consume)
    else if (this is Node.Elem) {
        consume(this)
        children.forEach { it.forEachElem(consume) }
    }
}

fun  Node.elements(): Sequence<Node.Elem> = sequence<Node.Elem> {
    if (this@elements is Node.Doc)
        yieldAll(docElement.elements())
    else if (this@elements is Node.Elem) {
        yield(this@elements)
        yieldAll(elementsChildren().flatMap(Node::elements))
    }
}

fun  Node.nodes(Attr: Boolean=true, Text: Boolean=true): Sequence<Node> = sequence {
    when (this@nodes) {
        is Node.Doc -> {
            yieldAll(docElement.nodes(Attr=Attr, Text=Text))
        }
        is Node.Elem -> {
            yield(this@nodes)
            if (Attr)
                yieldAll(attribs.values)
            yieldAll(allChildren().flatMap { it.nodes(Attr=Attr, Text=Text) })
        }
        is Node.Text -> {
            if (Text)
                yield(this@nodes)
        }
        else -> {
            require(false) { "Unexpected node type: ${this@nodes}"}
        }
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