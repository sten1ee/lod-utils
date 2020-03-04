package co.eft.xml

fun main(args: Array<String>) {
    // statement-2019-02.xml statement-2019-03.xml statement-2019-04.xml statement-2019-05.xml
    // statement-2019-06.xml statement-2019-07.xml statement-2019-08.xml statement-2019-09.xml
    // statement-2019-10.xml statement-2019-11.xml statement-2019-12.xml

    extractN3s(args.iterator())
        .filterNot(N3::isBankCommission)
        //.filter { it.cdtDbt == N3.CdtDbt.DBIT }
        //.sumByDouble { it.amount }
        //.printIt()
        .forEach { println(it) }
}
fun Double.printIt() = print(this)

fun N3.isBankCommission() = dsc.endsWith("COMM")


data class Account(val name: String)


private inline fun <reified T : Enum<T>> String.toEnum(): T = enumValueOf(this)

private fun Node.toAccount() = Account(textValue)

data class Party(val name: String, val acct: Account)

data class N3(val id:       Int,
              val amount:   Double,
              val n3ref:    String?,
              val cdtDbt:   CdtDbt,
              val dsc:      String,
              val dbtr:     Party,
              val cdtr:     Party)
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
      Party((n3/"NtryDtls/TxDtls/RltdPties/Dbtr/(Nm)?").textValue,
            (n3/"NtryDtls/TxDtls/RltdPties/DbtrAcct").toAccount()),
      Party((n3/"NtryDtls/TxDtls/RltdPties/Cdtr/(Nm)?").textValue,
            (n3/"NtryDtls/TxDtls/RltdPties/CdtrAcct").toAccount())
        )
}

/** Extract entries from a single statement file */
fun extractN3s(stmtFileName: String): Sequence<N3> =
    DocBuilder()
        .build(stmtFileName)
        .elements()
        .filter { it.name == "Ntry"}
        .map(::N3)

/** Extract entries from a collection of statement files */
fun extractN3s(fileNames: Iterator<String>): Sequence<N3> = sequence {
    fileNames.forEach() { yieldAll(extractN3s(it)) }
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