package com.sakurageto

import com.sakurageto.card.CardName
import com.sakurageto.card.CardSet.toCardData
import com.sakurageto.card.CardSet.toCardName
import com.sakurageto.gamelogic.GameVersion
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class HashMapTest {
    @Test
    fun hashMapTest(){
        val cardNameList = CardName.values()
        val exceptionSet = setOf(
            CardName.CARD_UNNAME, CardName.POISON_ANYTHING, CardName.SOLDIER_ANYTHING,
            CardName.PARTS_ANYTHING)

        for(cardName in cardNameList){
            if(cardName in exceptionSet) {
                continue
            }
            assertEquals(cardName, cardName.toCardData(GameVersion.VERSION_7_2).card_name)

            assertNotEquals(-1, cardName.toCardNumber(true))
            assertEquals(cardName, cardName.toCardNumber(true).toCardName())

            assertNotEquals(-1, cardName.toCardNumber(false))
            assertEquals(cardName, cardName.toCardNumber(false).toCardName())
        }
    }
}