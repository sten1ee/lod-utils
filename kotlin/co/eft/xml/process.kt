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