package io.iohk.ethereum.network.p2p.messages

import akka.util.ByteString
import io.iohk.ethereum.crypto._
import io.iohk.ethereum.domain.{Address, Receipt, TxLogEntry}
import io.iohk.ethereum.network.p2p.EthereumMessageDecoder
import io.iohk.ethereum.network.p2p.messages.PV63.Receipts
import io.iohk.ethereum.rlp.RLPImplicitConversions._
import io.iohk.ethereum.rlp.RLPImplicits._
import io.iohk.ethereum.rlp._
import org.scalatest.{FlatSpec, Matchers}
import org.spongycastle.util.encoders.Hex

class ReceiptsSpec extends FlatSpec with Matchers {

  val exampleHash = ByteString(kec256((0 until 32).map(_ => 1: Byte).toArray))
  val exampleLogsBloom = ByteString((0 until 256).map(_ => 1: Byte).toArray)

  val loggerAddress = Address(0xff)
  val logData = ByteString(Hex.decode("bb"))
  val logTopics = Seq(ByteString(Hex.decode("dd")), ByteString(Hex.decode("aa")))

  val exampleLog = TxLogEntry(loggerAddress, logTopics, logData)

  val cumulativeGas: BigInt = 0

  val receipt = Receipt(
    postTransactionStateHash = exampleHash,
    cumulativeGasUsed = cumulativeGas,
    logsBloomFilter = exampleLogsBloom,
    logs = Seq(exampleLog)
  )

  val receipts = Receipts(Seq(Seq(receipt)))

  val encodedReceipts =
    RLPList(RLPList(
      RLPList(
        exampleHash,
        cumulativeGas,
        exampleLogsBloom,
        RLPList(RLPList(loggerAddress.bytes, logTopics, logData))
      )))

  "Receipts" should "encode receipts" in {
    (receipts.toBytes: Array[Byte]) shouldBe encode(encodedReceipts)
  }

  it should "decode receipts" in {
    EthereumMessageDecoder.fromBytes(Receipts.code, encode(encodedReceipts), Versions.PV63) shouldBe receipts
  }

  it should "decode encoded receipts" in {
    EthereumMessageDecoder.fromBytes(Receipts.code, receipts.toBytes, Versions.PV63) shouldBe receipts
  }
}
