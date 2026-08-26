package nutribyte.service;

/**
 * Describes the result of a pantry quantity operation.
 */
public enum PantryOperationResult {
    SUCCESS,
    INVALID_QUANTITY,
    ITEM_NOT_FOUND,
    INSUFFICIENT_STOCK
}
