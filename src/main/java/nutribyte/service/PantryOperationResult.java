package nutribyte.service;

/**
 * Describes the result of a pantry quantity operation.
 */
public enum PantryOperationResult {
    SUCCESS,
    INVALID_QUANTITY,
    ITEM_NOT_FOUND,
    AMBIGUOUS_ITEM,
    INSUFFICIENT_STOCK,
    INVALID_INDEX,
    INVALID_FIELD,
    INVALID_VALUE
}
