package co.eft.xml

import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Text
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class DocBuilder {
    fun build(fileName: String): Node.Doc {
        val xmlFile = File(fileName)
        val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile)
        return build("<file-name:$fileName>", xmlDoc)
    }

    fun build(docName: String, xmlDoc: Document): Node.Doc {
        xmlDoc.documentElement.normalize()
        return Node.Doc(docName).apply {
            setDocElement(import(xmlDoc.documentElement) as Node.Elem)
        }
    }

    /** Import corresponding src node as an attrib or child within _this_ node
     *  (extension of co.eft.Node)
     */
    val TRIM_TEXT = true
    fun Node.import(src: org.w3c.dom.Node): Node =
        when (src) {
            is Text -> Node.Text(this, src.textContent.let { if (TRIM_TEXT) it.trim() else it })
            is Attr -> Node.Attr(this, src.name, src.value)
            is Element -> {
                Node.Elem(this, src.nodeName).apply {
                    setAttribs((0 until src.attributes.length).map { import(src.attributes.item(it)) as Node.Attr })
                    setChildren((0 until src.childNodes.length).map { import(src.childNodes.item(it)) })
                }
            }
            else -> throw IllegalStateException("Unexpected object type: ${src::class}")
        }
}
