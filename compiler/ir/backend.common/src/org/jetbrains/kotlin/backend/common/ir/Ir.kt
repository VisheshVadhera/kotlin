package org.jetbrains.kotlin.backend.common.ir

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.OperatorNameConventions
import javax.xml.stream.Location

// This is what Context collects about IR.
abstract class Ir<out T: CommonBackendContext>(val context: T, val irModule: IrModuleFragment) {

    abstract val symbols: Symbols<T>

    val defaultParameterDeclarationsCache = mutableMapOf<FunctionDescriptor, IrFunction>()

    open fun shouldGenerateHandlerParameterForDefaultBodyFun() = false
}

open class Symbols<out T: CommonBackendContext>(val context: T, private val symbolTable: SymbolTable) {

    private val builtIns
        get() = context.builtIns

    private fun builtInsPackage(vararg packageNameSegments: String) =
            context.builtIns.builtInsModule.getPackage(FqName.fromSegments(listOf(*packageNameSegments))).memberScope

//    val refClass = symbolTable.referenceClass(context.getInternalClass("Ref"))
//
//    val areEqualByValue = context.getInternalFunctions("areEqualByValue").map {
//        symbolTable.referenceSimpleFunction(it)
//    }
//
//    val areEqual = symbolTable.referenceSimpleFunction(context.getInternalFunctions("areEqual").single())
//
//    val ThrowNullPointerException = symbolTable.referenceSimpleFunction(
//            context.getInternalFunctions("ThrowNullPointerException").single())
//
//    val ThrowNoWhenBranchMatchedException = symbolTable.referenceSimpleFunction(
//            context.getInternalFunctions("ThrowNoWhenBranchMatchedException").single())

//    val ThrowTypeCastException = symbolTable.referenceSimpleFunction(
//            context.getInternalFunctions("ThrowTypeCastException").single())

    val ThrowUninitializedPropertyAccessException = symbolTable.referenceSimpleFunction(
            context.getInternalFunctions("ThrowUninitializedPropertyAccessException").single()
    )

    val stringBuilder = symbolTable.referenceClass(
            context.getClass(FqName("java.lang.StringBuilder")) as ClassDescriptor
    )

    val defaultConstructorMarker = symbolTable.referenceClass(context.getInternalClass("DefaultConstructorMarker"))

    val any = symbolTable.referenceClass(builtIns.any)
    val unit = symbolTable.referenceClass(builtIns.unit)

    val byte = symbolTable.referenceClass(builtIns.byte)
    val short = symbolTable.referenceClass(builtIns.short)
    val int = symbolTable.referenceClass(builtIns.int)
    val long = symbolTable.referenceClass(builtIns.long)

    val integerClasses = listOf(byte, short, int, long)

    val arrayOf = symbolTable.referenceSimpleFunction(
            builtInsPackage("kotlin").getContributedFunctions(
                    Name.identifier("arrayOf"), NoLookupLocation.FROM_BACKEND
            ).single()
    )

    val array = symbolTable.referenceClass(builtIns.array)

    private fun primitiveArrayClass(type: PrimitiveType) =
            symbolTable.referenceClass(builtIns.getPrimitiveArrayClassDescriptor(type))

    val byteArray = primitiveArrayClass(PrimitiveType.BYTE)
    val charArray = primitiveArrayClass(PrimitiveType.CHAR)
    val shortArray = primitiveArrayClass(PrimitiveType.SHORT)
    val intArray = primitiveArrayClass(PrimitiveType.INT)
    val longArray = primitiveArrayClass(PrimitiveType.LONG)
    val floatArray = primitiveArrayClass(PrimitiveType.FLOAT)
    val doubleArray = primitiveArrayClass(PrimitiveType.DOUBLE)
    val booleanArray = primitiveArrayClass(PrimitiveType.BOOLEAN)

    val arrays = PrimitiveType.values().map { primitiveArrayClass(it) } + array

    val copyRangeTo = arrays.map { symbol ->
        val classDescriptor = context.getClass(FqName("java.util.Arrays"))
        val functionDescriptor = classDescriptor.staticScope
                .getContributedFunctions(Name.identifier("copyOfRange"), NoLookupLocation.FROM_BACKEND)
                .first {
                    it.valueParameters[0].type?.constructor?.declarationDescriptor == symbol.descriptor
                }
        symbol.descriptor to symbolTable.referenceSimpleFunction(functionDescriptor)
    }.toMap()

    val intAnd = symbolTable.referenceFunction(
            builtIns.intType.memberScope
                    .getContributedFunctions(OperatorNameConventions.AND, NoLookupLocation.FROM_BACKEND)
                    .single()
    )

    val intPlusInt = symbolTable.referenceFunction(
            builtIns.intType.memberScope
                    .getContributedFunctions(OperatorNameConventions.PLUS, NoLookupLocation.FROM_BACKEND)
                    .single {
                        it.valueParameters.single().type == builtIns.intType
                    }
    )

//
//    val valuesForEnum = symbolTable.referenceSimpleFunction(
//            context.getInternalFunctions("valuesForEnum").single())
//
//    val valueOfForEnum = symbolTable.referenceSimpleFunction(
//            context.getInternalFunctions("valueOfForEnum").single())
//
//    val getContinuation = symbolTable.referenceSimpleFunction(
//            context.getInternalFunctions("getContinuation").single())

//    val coroutineImpl = symbolTable.referenceClass(context.getInternalClass("CoroutineImpl"))

//    val coroutineSuspendedGetter = symbolTable.referenceSimpleFunction(
//            builtInsPackage("kotlin", "coroutines", "experimental", "intrinsics")
//                    .getContributedVariables(Name.identifier("COROUTINE_SUSPENDED"), NoLookupLocation.FROM_BACKEND)
//                    .single().getter!!
//    )

    val kFunctionImpl = symbolTable.referenceClass(context.reflectionTypes.kFunctionImpl)

    val kProperty0Impl = symbolTable.referenceClass(context.reflectionTypes.kProperty0Impl)
    val kProperty1Impl = symbolTable.referenceClass(context.reflectionTypes.kProperty1Impl)
    val kProperty2Impl = symbolTable.referenceClass(context.reflectionTypes.kProperty2Impl)
    val kMutableProperty0Impl = symbolTable.referenceClass(context.reflectionTypes.kMutableProperty0Impl)
    val kMutableProperty1Impl = symbolTable.referenceClass(context.reflectionTypes.kMutableProperty1Impl)
    val kMutableProperty2Impl = symbolTable.referenceClass(context.reflectionTypes.kMutableProperty2Impl)
//    val kLocalDelegatedPropertyImpl = symbolTable.referenceClass(context.reflectionTypes.kLocalDelegatedPropertyImpl)
//    val kLocalDelegatedMutablePropertyImpl = symbolTable.referenceClass(context.reflectionTypes.kLocalDelegatedMutablePropertyImpl)

}