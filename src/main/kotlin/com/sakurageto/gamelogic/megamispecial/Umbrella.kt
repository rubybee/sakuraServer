package com.sakurageto.gamelogic.megamispecial

enum class Umbrella(){
    FOLD,
    UNFOLD;

    fun opposite(): Umbrella {
        return if (this == FOLD){
            UNFOLD
        } else{
            FOLD
        }
    }
}