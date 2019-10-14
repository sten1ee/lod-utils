package co.eft.xml



fun main(args: Array<String>) {
    val doc = DocBuilder().build("contributors/contributors.xml")
    println(doc)
}
