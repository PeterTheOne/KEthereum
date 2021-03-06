package org.kethereum.eip712

/**
 *
 * This is pm.gnosis.eip712 adapted to kethereum types
 *
 */

import com.squareup.moshi.Moshi
import okio.buffer
import okio.source
import java.io.InputStream

class MoshiAdapter : EIP712JsonAdapter {
    private val adapter by lazy { Moshi.Builder().build().adapter(TypedData::class.java) }

    override fun parse(typedDataJson: String): EIP712JsonAdapter.Result {
        val typedData = adapter.fromJson(typedDataJson) ?: throw IllegalArgumentException("Json does not represent TypedData")
        return parse(typedData)
    }

    override fun parse(inputStream: InputStream): EIP712JsonAdapter.Result =
        inputStream.source().buffer().use { bufferedSource ->
            val typedData = adapter.fromJson(bufferedSource) ?: throw IllegalArgumentException("Json does not represent TypedData")
            return parse(typedData)
        }

    private fun parse(typedData: TypedData): EIP712JsonAdapter.Result {
        return EIP712JsonAdapter.Result(
            primaryType = typedData.primaryType,
            domain = typedData.domain,
            message = typedData.message,
            types = typedData.types.mapValues { (_, types) -> types.map { EIP712JsonAdapter.Parameter(it.name, it.type) } }
        )
    }

    data class TypedData(
        val types: Map<String, List<TypeParam>>,
        val primaryType: String,
        val domain: Map<String, Any>,
        val message: Map<String, Any>
    )

    data class TypeParam(
        val name: String,
        val type: String
    )
}
