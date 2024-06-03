package com.sakurageto.card.basicenum

enum class PlayerEnum {
    PLAYER1,
    PLAYER2;

    fun opposite(): PlayerEnum {
        return if(this == PLAYER1){
            PLAYER2
        } else{
            PLAYER1
        }
    }
}