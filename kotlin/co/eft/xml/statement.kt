package co.eft.xml

fun main(args: Array<String>) {
    // statement-2019-02.xml statement-2019-03.xml statement-2019-04.xml statement-2019-05.xml
    // statement-2019-06.xml statement-2019-07.xml statement-2019-08.xml statement-2019-09.xml
    // statement-2019-10.xml statement-2019-11.xml statement-2019-12.xml

    extractN3s(args.iterator())
        .selectAny(
            N3::isBankTax,
            N3::isOtherBankTax,
            N3::isATMDebit,
            N3::isATMDebitTax,
            N3::isMonthlyBankTax,
            N3::isOwnerDebit,
            N3::isBgPosDebit,
            N3::isNapDebit,
            N3::isReceiptDebit,
            N3::isPayrollDebit

            //N3::isOwnerCredit,
            //N3::isIncomeCredit
        )
        //.sumByDouble { it.amount }
        //.printIt()
        .onEach { println(it) }
        .sumByDouble { it.amount }
        .also { println("%.2f".format(it)) }
}

fun <T> Sequence<T>.select(predicate: (T) -> Boolean): Sequence<T> = filter(predicate)

fun <T> Sequence<T>.selectAny(vararg predicates: (T) -> Boolean): Sequence<T> =
    filter { t -> predicates.any { p -> p(t) }}

fun <T> print(it: T): T { println(it); return it}

fun N3.isBankTax() =
               isDebit()
            && dsc.endsWith("COMM")

fun N3.isOtherBankTax() =
               isDebit()
            && dbtr.isBlank()
            && cdtr.isBlank()

fun N3.isATMDebit() =
               isDebit()
            && dsc.startsWith("Теглене от АТМ ")
            && !dsc.endsWith("-Такса")

fun N3.isATMDebitTax() =
               isDebit()
            && dsc.startsWith("Теглене от АТМ ")
            && dsc.endsWith("-Такса")

fun N3.isMonthlyBankTax() =
               isDebit()
            && dsc == "Месечна такса SMS известие"

fun N3.isOwnerDebit() =
               isDebit()
            && cdtr.acctNr == "BG34FINV91501016623437"

fun N3.isOwnerCredit() =
               isCredit()
            && dbtr.acctNr == "BG34FINV91501016623437"
            && cdtr.acctNr == "BG63RZBB91551010205632"

fun N3.isIncomeCredit() =
               isCredit()
            && dbtr.acctNr != "BG34FINV91501016623437"
            && cdtr.acctNr == "BG63RZBB91551010205632"

fun N3.isBgPosDebit() =
               isDebit()
            && dsc == "Плащане при БГ търговец"

fun N3.isNapDebit() =
               isDebit()
            && cdtr.name.startsWith("ТД НА НАП ") // assert this
            && cdtr.acctNr.indexOf("UBBS88888") == 4
            && cdtr.acctNr.endsWith("00")

fun N3.isReceiptDebit() =
               isDebit()
            && (dsc.startsWith("ПЛАЩАНЕ ПО ФАКТУРА ")
              || dsc.startsWith("ПЛАЩАНЕ ФАКТУРА ")
              || dsc.startsWith("ПЛАЩАНЕ ФАКТУРИ "))

fun N3.isPayrollDebit() =
               isDebit()
            && (dsc.startsWith("ЗАПЛАТА ")
              || dsc.startsWith("ЗАПЛАТИ "))

data class Account(val name: String)


private inline fun <reified T : Enum<T>> String.toEnum(): T = enumValueOf(this)

data class Party(val name: String, val acctNr: String) {
    fun  isBlank() = (name.isBlank() && acctNr.isBlank())
}

data class N3(val id:       Int,
              val amount:   Double,
              val n3ref:    String?,
              val cdtDbt:   CdtDbt,
              val dsc:      String,
              val dbtr:     Party,
              val cdtr:     Party,
              val valDt:    String,
              val bookDt:   String)
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
            (n3/"NtryDtls/TxDtls/RltdPties/DbtrAcct").textValue),
      Party((n3/"NtryDtls/TxDtls/RltdPties/Cdtr/(Nm)?").textValue,
            (n3/"NtryDtls/TxDtls/RltdPties/CdtrAcct").textValue),
            (n3/"ValDt/Dt/#").value,
            (n3/"BookgDt/Dt/#").value
        )

    fun isDebit() = cdtDbt == CdtDbt.DBIT
    fun isCredit() = cdtDbt == CdtDbt.CRDT
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