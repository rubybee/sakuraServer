package com.sakurageto.protocol

enum class CommandEnum(var real_number: Int) {
    SELECT_MODE(1),
    END_OF_SELECTMODE(2),
    SELECT_MEGAMI(3),
    END_OF_SELECT_MEGAMI(4),
    CHECK_MEGAMI(5),
    SELECT_BAN(6),
    END_SELECT_BAN(7),
    CHECK_YOUR(8),
    CHECK_ANOTHER(9),
}