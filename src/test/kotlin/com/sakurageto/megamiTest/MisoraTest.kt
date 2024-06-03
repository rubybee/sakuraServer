package com.sakurageto.megamiTest

import com.sakurageto.ApplicationTest
import com.sakurageto.card.*
import com.sakurageto.card.basicenum.CardClass
import com.sakurageto.card.basicenum.CardType
import com.sakurageto.card.basicenum.PlayerEnum
import com.sakurageto.card.basicenum.SubType
import com.sakurageto.card.basicenum.MegamiEnum
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class MisoraTest: ApplicationTest() {
    private suspend fun useCardPerjury(playerEnum: PlayerEnum, card_name: CardName, original_card: Int, location: LocationEnum){
        when(location){
            LocationEnum.HAND -> {
                gameStatus.getCardFrom(playerEnum, original_card, LocationEnum.HAND)?.let {
                    gameStatus.useCardPerjury(playerEnum, it, card_name.toCardNumber(true), LocationEnum.HAND)
                }
            }

            LocationEnum.READY_SOLDIER_ZONE -> {
                gameStatus.getCardFrom(playerEnum, original_card, LocationEnum.READY_SOLDIER_ZONE)?.let {
                    gameStatus.useCardPerjury(playerEnum, it, card_name.toCardNumber(true), LocationEnum.READY_SOLDIER_ZONE)
                }
            }
            else -> {}
        }
    }

    @Before
    fun setting(){
        gameStatus.player1.megamiOne = MegamiEnum.MISORA
        MegamiEnum.MISORA.settingForOriginal(PlayerEnum.PLAYER1, gameStatus)
    }

    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.MISORA_BOW_SPILLING, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.MISORA_AIMING_KICK, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.MISORA_WIND_HOLE, CardClass.NORMAL, CardType.ATTACK, SubType.REACTION)
        cardTypeTest(CardName.MISORA_GAP_SI_EUL_SI, CardClass.NORMAL, CardType.ATTACK, SubType.FULL_POWER)
        cardTypeTest(CardName.MISORA_PRECISION, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.MISORA_TRACKING_ATTACK, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.MISORA_SKY_WING, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.MISORA_ENDLESS_END, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.MISORA_CLOUD_EMBROIDERED_CLOUD, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.MISORA_SHADOW_SHADY_SHADOW, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.REACTION)
        cardTypeTest(CardName.MISORA_SKY_BEYOND_SKY, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.FULL_POWER)
    }

    @Test
    fun aimingTest() = runTest {
        resetValue(5, 5, 10, 10, 4, 5)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        gameStatus.endPhase()

        assertEquals(4, gameStatus.player1.aiming)

        gameStatus.endPhase()
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.FULL_POWER_NO))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_END_TURN))
        gameStatus.mainPhase()

        assertEquals(null, gameStatus.player1.aiming)
    }

    @Test
    fun bowSpillingTest() = runTest {
        resetValue(0, 5, 10, 10, 4, 1)

        gameStatus.player1.aiming = 4
        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.MISORA_BOW_SPILLING, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MISORA_BOW_SPILLING, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(1, gameStatus.player1.aura)
        assertEquals(0, gameStatus.dust)
    }

    @Test
    fun aimingKickTest() = runTest {
        resetValue(0, 1, 10, 10, 4, 1)
        gameStatus.player1.aiming = 4

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.MISORA_AIMING_KICK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MISORA_AIMING_KICK, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(5, gameStatus.player1.aiming)
    }

    @Test
    fun windHoleTestEqual() = runTest {
        resetValue(0, 0, 10, 10, 3, 1)
        gameStatus.player1.aiming = 3

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.MISORA_WIND_HOLE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MISORA_WIND_HOLE, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(1, gameStatus.player1.aura)
        assertEquals(0, gameStatus.dust)
    }

    @Test
    fun windHoleTestBigger() = runTest {
        resetValue(0, 1, 10, 10, 3, 0)
        gameStatus.player1.aiming = 2

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.MISORA_WIND_HOLE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MISORA_WIND_HOLE, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.aura)
        assertEquals(2, gameStatus.distanceToken)
        assertEquals(2, gameStatus.dust)
    }

    @Test
    fun windHoleTestSmaller() = runTest {
        resetValue(0, 0, 10, 10, 3, 1)
        gameStatus.player1.aiming = 4

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.MISORA_WIND_HOLE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MISORA_WIND_HOLE, LocationEnum.HAND)

        assertEquals(4, gameStatus.distanceToken)
        assertEquals(0, gameStatus.dust)
    }

    @Test
    fun gapSiEulSiTest() = runTest {
        resetValue(0, 4, 10, 10, 2, 1)
        gameStatus.player1.aiming = 5
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.DECK)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.MISORA_GAP_SI_EUL_SI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MISORA_GAP_SI_EUL_SI, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.COVER_CARD))
        assertEquals(1, gameStatus.player2.coverCard.size)
    }

    @Test
    fun gapSiEulSiDeckTest() = runTest {
        resetValue(0, 4, 10, 10, 2, 1)
        gameStatus.player1.aiming = 5
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_GIBACK, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.DECK)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.DECK)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_GUHAB, LocationEnum.DECK)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.MISORA_GAP_SI_EUL_SI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MISORA_GAP_SI_EUL_SI, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(3, gameStatus.player2.coverCard.size)
    }

    @Test
    fun precisionTest() = runTest {
        resetValue(0, 2, 10, 10, 3, 1)
        gameStatus.player1.aiming = 3

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.MISORA_PRECISION, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MISORA_PRECISION, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)
        assertEquals(1, gameStatus.player1.concentration)
    }

    @Test
    fun trackingAttackTest() = runTest {
        resetValue(0, 4, 10, 10, 8, 1)
        gameStatus.startTurnDistance = 6
        gameStatus.player1.aiming = 6
        gameStatus.player1.unselectedCard.add(CardName.HAGANE_CENTRIFUGAL_ATTACK)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_HAGANE_CENTRIFUGAL_ATTACK
        )))

        addCard(PlayerEnum.PLAYER1, CardName.MISORA_TRACKING_ATTACK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MISORA_TRACKING_ATTACK, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)
        assertEquals(false, CardName.HAGANE_CENTRIFUGAL_ATTACK in gameStatus.player1.unselectedCard)
    }

    @Test
    fun trackingAttackCoverTest() = runTest {
        resetValue(0, 4, 10, 10, 8, 1)
        gameStatus.player1.aiming = 7
        addCard(PlayerEnum.PLAYER1, CardName.MISORA_BOW_SPILLING, LocationEnum.COVER_CARD)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_MISORA_BOW_SPILLING
        )))

        addCard(PlayerEnum.PLAYER1, CardName.MISORA_TRACKING_ATTACK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MISORA_TRACKING_ATTACK, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.MISORA_TRACKING_ATTACK, LocationEnum.DISCARD_YOUR))
    }

    @Test
    fun skyWingTest() = runTest {
        resetValue(0, 2, 10, 10, 3, 3)

        addCard(PlayerEnum.PLAYER1, CardName.MISORA_SKY_WING, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MISORA_SKY_WING, LocationEnum.HAND)

        assertEquals(5, gameStatus.distanceToken)
        assertEquals(0, gameStatus.player2.aura)

        startPhase()
        startPhase()

        assertEquals(6, gameStatus.getAdjustDistance())
        assertEquals(3, gameStatus.getAdjustSwellDistance())
    }

    @Test
    fun endlessEndTest() = runTest {
        resetValue(0, 2, 10, 10, 3, 3)
        gameStatus.player1.aiming = 3; gameStatus.player1.flare = 2

        addCard(PlayerEnum.PLAYER1, CardName.MISORA_ENDLESS_END, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.MISORA_ENDLESS_END, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(9, gameStatus.player2.life)

        gameStatus.endPhase()
        gameStatus.player1.flare = 2; gameStatus.player1.aiming = 5
        useCard(PlayerEnum.PLAYER1, CardName.MISORA_ENDLESS_END, LocationEnum.SPECIAL_CARD)

        assertEquals(8, gameStatus.player2.life)

        gameStatus.endPhase()
        gameStatus.endPhase()
        gameStatus.player1.flare = 2; gameStatus.player1.aiming = 7
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.DECK)
        useCard(PlayerEnum.PLAYER1, CardName.MISORA_ENDLESS_END, LocationEnum.SPECIAL_CARD)

        assertEquals(7, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.COVER_CARD))
    }

    @Test
    fun cloudEmbroideredCloudTest() = runTest {
        resetValue(0, 2, 10, 10, 10, 3)
        gameStatus.player1.flare = 1; gameStatus.player1.aiming = 1

        addCard(PlayerEnum.PLAYER1, CardName.MISORA_CLOUD_EMBROIDERED_CLOUD, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.MISORA_CLOUD_EMBROIDERED_CLOUD, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(1, gameStatus.getAdjustDistance())

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_GHOST_LINK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_GHOST_LINK, LocationEnum.HAND)

        assertEquals(2, gameStatus.getAdjustDistance())
    }

    @Test
    fun shadowShadyShadowTest() = runTest {
        resetValue(0, 2, 10, 10, 2, 3)
        gameStatus.turnPlayer = PlayerEnum.PLAYER2
        gameStatus.player2.megamiOne = MegamiEnum.RENRI
        MegamiEnum.RENRI.settingForOriginal(PlayerEnum.PLAYER2, gameStatus)
        gameStatus.player1.flare = 2; gameStatus.player1.aiming = 1

        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_NOT))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NOT))
        addCard(PlayerEnum.PLAYER1, CardName.MISORA_SHADOW_SHADY_SHADOW, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER1, CardName.MISORA_SHADOW_SHADY_SHADOW, LocationEnum.SPECIAL_CARD)

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.RENRI_FALSE_STAB, LocationEnum.HAND)
        useCardPerjury(PlayerEnum.PLAYER2, CardName.RENRI_FALSE_STAB, NUMBER_YURINA_CHAM + SECOND_PLAYER_START_NUMBER, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(10, gameStatus.player1.life)

        startPhase(); startPhase(); startPhase()

        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR))
    }

    @Test
    fun skyBeyondSkyTest() = runTest {
        resetValue(0, 1, 10, 10, 5, 3)
        gameStatus.player1.flare = 5; gameStatus.player2.flare = 1
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.MISORA_SKY_BEYOND_SKY, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.MISORA_SKY_BEYOND_SKY, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(10, gameStatus.getAdjustDistance())

        startPhase(); startPhase()

        assertEquals(8, gameStatus.getAdjustDistance())
        assertEquals(0, gameStatus.player2.aura)
        assertEquals(0, gameStatus.player2.flare)
        assertEquals(9, gameStatus.player2.life)

        gameStatus.player1.flare = 5
        useCard(PlayerEnum.PLAYER1, CardName.MISORA_SKY_BEYOND_SKY, LocationEnum.YOUR_USED_CARD)

        assertEquals(8, gameStatus.getAdjustDistance())
    }
}