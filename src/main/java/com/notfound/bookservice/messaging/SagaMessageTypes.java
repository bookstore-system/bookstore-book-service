package com.notfound.bookservice.messaging;

public final class SagaMessageTypes {

    public static final String RESERVE_COMMAND = "book.stock.reserve.command";
    public static final String CONFIRM_COMMAND = "book.stock.confirm.command";
    public static final String RELEASE_COMMAND = "book.stock.release.command";

    public static final String RESERVED_EVENT = "book.stock.reserved";
    public static final String CONFIRMED_EVENT = "book.stock.confirmed";
    public static final String RELEASED_EVENT = "book.stock.released";
    public static final String FAILED_EVENT = "book.stock.failed";
    public static final String CHANGED_EVENT = "book.stock.changed";

    public static final String RK_RESERVE_COMMAND = "book.stock.reserve.command";
    public static final String RK_CONFIRM_COMMAND = "book.stock.confirm.command";
    public static final String RK_RELEASE_COMMAND = "book.stock.release.command";

    public static final String RK_RESERVED_EVENT = "book.stock.reserved";
    public static final String RK_CONFIRMED_EVENT = "book.stock.confirmed";
    public static final String RK_RELEASED_EVENT = "book.stock.released";
    public static final String RK_FAILED_EVENT = "book.stock.failed";
    public static final String RK_CHANGED_EVENT = "book.stock.changed";

    private SagaMessageTypes() {}
}
