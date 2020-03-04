package co.eft.xml

fun main(args: Array<String>) {
    // statement-2019-02.xml statement-2019-03.xml statement-2019-04.xml statement-2019-05.xml
    // statement-2019-06.xml statement-2019-07.xml statement-2019-08.xml statement-2019-09.xml
    // statement-2019-10.xml statement-2019-11.xml statement-2019-12.xml

    extractNtries(args.iterator())
        .filter(Ntry::isBankCommission)
        //.sumByDouble { it.amount }
        //.printIt()
        .forEach { println(it) }
}
fun Double.printIt(format: String="value is $this") = println(format)

fun Ntry.isBankCommission() = dsc.endsWith("COMM")


data class Account(val name: String)


private inline fun <reified T : Enum<T>> String.toEnum(): T = enumValueOf(this)

private fun Node.toAccount() = Account(textValue)

data class Ntry(val id:     Int,
                val amount: Double,
                val n3ref:  String?,
                val cdtDbt: CdtDbt,
                val dsc:    String,
                val src:    Account,
                val dst:    Account)
{
    enum class CdtDbt {
        CRDT,
        DBIT
    }

    enum class Type {
        FEE_ONCE,
        FEE_MONTHLY,
        FEE_TRANSFER,

        OWNER_CRDT,
        OWNER_CRDT_PAYBACK,
        OWNER_DBIT_ATM,
        OWNER_DBIT_TRANSFER,
    }

    companion object {
        private var lastId = 0
    }

    constructor(n3: Node.Elem)
        :this(
            ++lastId,
            (n3/"Amt/#").value.toDouble(),
            (n3/"(NtryRef)?/#").value,
            (n3/"CdtDbtInd/#").value.toEnum<CdtDbt>(),
            (n3/"NtryDtls/TxDtls/RmtInf/Ustrd/#").value,
            (n3/"NtryDtls/TxDtls/RltdPties/DbtrAcct").toAccount(),
            (n3/"NtryDtls/TxDtls/RltdPties/CdtrAcct").toAccount()
        )
}

/** Extract entries from a single statement file */
fun extractNtries(stmtFileName: String): Sequence<Ntry> =
    DocBuilder()
        .build(stmtFileName)
        .elements()
        .filter { it.name == "Ntry"}
        .map(::Ntry)

/** Extract entries from a collection of statement files */
fun extractNtries(fileNames: Iterator<String>): Sequence<Ntry>  = sequence {
    fileNames.forEach() { yieldAll(extractNtries(it)) }
}

interface Person {
    val name: String
    val surname: String
    val age: Int
}

data class Student(
    override val name: String,
    override val surname: String,
    override val age: Int,
    val university: String
) : Person

data class Worker(
    override val name: String,
    override val surname: String,
    override val age: Int,
    val company: String
) : Person