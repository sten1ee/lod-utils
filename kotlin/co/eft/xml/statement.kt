package co.eft.xml

fun main(args: Array<String>) {
    processStatementFile("/Users/stenlee/Downloads/statement-2019-11.xml")
}

data class Account(val name: String)

enum class Type {
    CRDT,
    DBIT
}

private inline fun <reified T : Enum<T>> String.toEnum(): T = enumValueOf(this)

private fun Node.toAccount() = Account(textValue)

data class Entry(val amount: Double, val n3ref: String?, val type: Type, val description: String, val src: Account, val dst: Account) {

    constructor(n3: Node.Elem)
        :this(
            (n3/"Amt/#").value.toDouble(),
            (n3/"(NtryRef)?/#").value,
            (n3/"CdtDbtInd/#").value.toEnum<Type>(),
            (n3/"NtryDtls/TxDtls/RmtInf/Ustrd/#").value,
            (n3/"NtryDtls/TxDtls/RltdPties/DbtrAcct").toAccount(),
            (n3/"NtryDtls/TxDtls/RltdPties/CdtrAcct").toAccount()
        )
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