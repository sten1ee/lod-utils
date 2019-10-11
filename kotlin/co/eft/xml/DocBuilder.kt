package co.eft.xml

import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Text
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class DocBuilder {
    fun parse(fileName: String): Node.Doc {
        val xmlFile = File(fileName)
        val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile)
        return buildNodeDoc(xmlDoc, "<file-name:$fileName>")
    }

    fun buildNodeDoc(xmlDoc: Document, nodeDocName: String): Node.Doc {
        xmlDoc.documentElement.normalize()
        val nodeDoc = Node.Doc(nodeDocName)
        nodeDoc.element = xmlDoc.documentElement.translate(nodeDoc) as Node.Elem
        return nodeDoc
    }

    fun org.w3c.dom.Node.translate(parent: Node): Node =
        when (this) {
            is Text -> Node.Text(parent, textContent)
            is Attr -> Node.Attr(parent, name, value)
            is Element -> {
                Node.Elem(parent, nodeName).apply {
                    setAttribs((0 until attributes.length).map { attributes.item(it).translate(this) as Node.Attr })
                    setChildren((0 until childNodes.length).map { childNodes.item(it).translate(this) })
                }
            }
            else -> null!!
        }
}


fun main(args: Array<String>) {
    val doc = DocBuilder().parse("contributors/contributors.xml")
    println(doc)
}
