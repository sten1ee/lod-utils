package co.eft.xml

fun main(args: Array<String>) {
    val re = Regex("[^\\p{L}\\d\\s.,‘’()/-]")

    processStatementFile("/Users/stenlee/Downloads/statement-2019-11.xml")
}

data class Account(val iban: String) {
}

enum class Type {
    CRDT,
    DBIT
}

private fun String.toType() = Type.valueOf(this)

data class Entry(val amount: Double, val n3ref: String?, val type: Type, val description: String, val src: Account, val dst: Account) {

    constructor(n3: Node.Elem)
        :this(
            (n3/"Amt"/'#').toDouble(),
            (n3/"(NtryRef)?"/'#'),
            (n3/"CdtDbtInd"/'#').toType(),
            (n3/"NtryDtls"/"TxDtls"/"RmtInf"/"Ustrd"/'#'),
            Account("srcAccountOf(n3)"), Account("dstAccountOf(n3))"))
}

fun processStatementFile(fileName: String): Unit {
    val doc = DocBuilder().build(fileName)


    val elements = doc .elements().toList()
    elements
        .filter { it.name == "Ntry"}
        .map { Entry(it) }
        .forEach { println(it) }
}

abstract class Person(
    val name: String,
    val surname: String,
    val age: Int
)

class Student(
    name: String,
    surname: String,
    age: Int,
    val university: String
) : Person(name, surname, age)

class Worker(
    name: String,
    surname: String,
    age: Int,
    val company: String
) : Person(name, surname, age)