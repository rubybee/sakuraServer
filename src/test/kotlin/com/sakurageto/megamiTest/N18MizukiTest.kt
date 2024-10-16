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

class N18MizukiTest: ApplicationTest() {
    @Before
    fun setting(){
        gameStatus.player1.megamiOne = MegamiEnum.MIZUKI
        MegamiEnum.MIZUKI.settingForOriginal(PlayerEnum.PLAYER1, gameStatus)
    }

    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.MIZUKI_JIN_DU, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.MIZUKI_BAN_GONG, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.MIZUKI_SHOOTING_DOWN, CardClass.NORMAL, CardType.ATTACK, SubType.REACTION)
        cardTypeTest(CardName.MIZUKI_HO_LYEONG, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.MIZUKI_BANG_BYEOG, CardClass.NORMAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.MIZUKI_OVERPOWERING_GO_FORWARD, CardClass.NORMAL, CardType.BEHAVIOR, SubType.FULL_POWER)
        cardTypeTest(CardName.MIZUKI_JEON_JANG, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.MIZUKI_HACHIRYU_CHEONJUGAK, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.REACTION)
        cardTypeTest(CardName.MIZUKI_HIJAMARU_TRIPLET, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.MIZUKI_TARTENASHI_DAESUMUN, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.MIZUKI_MIZUKI_BATTLE_CRY, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.FULL_POWER)

        cardTypeTest(CardName.KODAMA_TU_SIN, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.SOLDIER_SPEAR_1, CardClass.SOLDIER, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.SOLDIER_SPEAR_2, CardClass.SOLDIER, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.SOLDIER_SHIELD, CardClass.SOLDIER, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.SOLDIER_HORSE, CardClass.SOLDIER, CardType.ENCHANTMENT, SubType.NONE)
    }

    @Test
    fun lastTurnReactTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 0)

        addCard(PlayerEnum.PLAYER2, CardName.TOKOYO_WOOAHHANTAGUCK, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.TOKOYO_WOOAHHANTAGUCK, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)

        gameStatus.endPhase()
        assertEquals(true, gameStatus.player2.lastTurnReact)
    }

    @Test
    fun jinDuTest() = runTest{
        resetValue(0, 0, 10, 10, 2, 0)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_SOLDIER_SPEAR_1
        )))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.MIZUKI_JIN_DU, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MIZUKI_JIN_DU, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.SOLDIER_SPEAR_1, LocationEnum.READY_SOLDIER_ZONE))
    }

    @Test
    fun banGongTest() = runTest {
        resetValue(0, 3, 10, 10, 2, 0)
        gameStatus.player1.fullAction = true; gameStatus.player1.lastTurnReact = true

        addCard(PlayerEnum.PLAYER1, CardName.MIZUKI_BAN_GONG, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MIZUKI_BAN_GONG, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun shootingDownTest() = runTest {
        resetValue(2, 3, 10, 10, 3, 0)

        startPhase()

        addCard(PlayerEnum.PLAYER1, CardName.MIZUKI_SHOOTING_DOWN, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER1, CardName.MIZUKI_SHOOTING_DOWN, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)

        assertEquals(10, gameStatus.player1.life)
    }

    @Test
    fun hoLyeongTest() = runTest {
        resetValue(0, 3, 10, 10, 2, 0)
        gameStatus.player1.lastTurnReact = true

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_SOLDIER_SPEAR_1
        )))

        addCard(PlayerEnum.PLAYER1, CardName.MIZUKI_HO_LYEONG, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MIZUKI_HO_LYEONG, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.concentration)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.SOLDIER_SPEAR_1, LocationEnum.READY_SOLDIER_ZONE))
    }

    @Test
    fun bangByeogTest() = runTest {
        resetValue(0, 1, 10, 10, 3, 0)

        addCard(PlayerEnum.PLAYER2, CardName.MIZUKI_BANG_BYEOG, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.MIZUKI_BANG_BYEOG, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)

        addCard(PlayerEnum.PLAYER2, CardName.MIZUKI_BANG_BYEOG, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.MIZUKI_BANG_BYEOG, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun overPoweringGoForwardTest() = runTest {
        resetValue(0, 1, 10, 10, 5, 5)
        gameStatus.player1.fullAction = true

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_SOLDIER_SPEAR_1
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_THREE))

        addCard(PlayerEnum.PLAYER1, CardName.MIZUKI_OVERPOWERING_GO_FORWARD, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MIZUKI_OVERPOWERING_GO_FORWARD, LocationEnum.HAND)

        assertEquals(2, gameStatus.player1.aura)
        assertEquals(4, gameStatus.distanceToken)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.SOLDIER_SPEAR_1, LocationEnum.READY_SOLDIER_ZONE))
    }

    @Test
    fun jeonJangTest() = runTest {
        resetValue(0, 1, 10, 10, 6, 5)

        addCard(PlayerEnum.PLAYER1, CardName.MIZUKI_JEON_JANG, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MIZUKI_JEON_JANG, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_REDBULLET, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_REDBULLET, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_SHOOT, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_SHOOT, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun hachiryuCheonJuGakTest() = runTest {
        resetValue(0, 1, 10, 10, 4, 5)
        gameStatus.player2.flare = 5

        addCard(PlayerEnum.PLAYER2, CardName.MIZUKI_HACHIRYU_CHEONJUGAK, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.MIZUKI_HACHIRYU_CHEONJUGAK, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_STAR_NAIL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_STAR_NAIL, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.flare)
        assertEquals(10, gameStatus.player2.life)

        gameStatus.endPhase(); startPhase()

        addCard(PlayerEnum.PLAYER2, CardName.HIMIKA_SHOOT, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.HIMIKA_SHOOT, LocationEnum.HAND)

        assertEquals(8, gameStatus.player1.life)
    }

    @Test
    fun hijamaruTripletTest() = runTest {
        suspend fun returnTest() {
            addCard(PlayerEnum.PLAYER1, CardName.MIZUKI_BANG_BYEOG, LocationEnum.HAND)
            useCard(PlayerEnum.PLAYER1, CardName.MIZUKI_BANG_BYEOG, LocationEnum.HAND)

            assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.MIZUKI_HIJAMARU_TRIPLET, LocationEnum.SPECIAL_CARD))
        }
        resetValue(0, 2, 10, 10, 4, 5)
        gameStatus.player1.flare = 2

        addCard(PlayerEnum.PLAYER1, CardName.MIZUKI_HIJAMARU_TRIPLET, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.MIZUKI_HIJAMARU_TRIPLET, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(9, gameStatus.player2.life)

        returnTest()
    }

    @Test
    fun tartenashiDaesumunTest() = runTest {
        resetValue(0, 2, 10, 10, 4, 5)
        gameStatus.player1.flare = 3

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_INSTALLATION, mutableListOf(
            NUMBER_SAINE_BETRAYAL
        )))

        addCard(PlayerEnum.PLAYER1, CardName.KODAMA_TU_SIN, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_BETRAYAL, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.MIZUKI_TARTENASHI_DAESUMUN, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.MIZUKI_TARTENASHI_DAESUMUN, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.KODAMA_TU_SIN, LocationEnum.READY_SOLDIER_ZONE))
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.SAINE_BETRAYAL, LocationEnum.READY_SOLDIER_ZONE))

        gameStatus.endPhase(); startPhase()

        addCard(PlayerEnum.PLAYER1, CardName.MIZUKI_BANG_BYEOG, LocationEnum.READY_SOLDIER_ZONE)
        useCard(PlayerEnum.PLAYER1, CardName.MIZUKI_BANG_BYEOG, LocationEnum.READY_SOLDIER_ZONE)

        resetValue(0, 1, 10, 10, 4, 4)
        addReactData(PlayerEnum.PLAYER2)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_BETRAYAL, LocationEnum.READY_SOLDIER_ZONE)

        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun mizukiBattleCryTest() = runTest {
        resetValue(0, 3, 10, 10, 4, 5)
        gameStatus.player1.flare = 5; gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_GUHAB, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_SUNSTAGE, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.MIZUKI_MIZUKI_BATTLE_CRY, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.MIZUKI_MIZUKI_BATTLE_CRY, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)

        gameStatus.player1.fullAction = false

        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_SUNSTAGE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_GUHAB, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)
        assertEquals(2, gameStatus.player1.concentration)
        assertEquals(true, gameStatus.player1.endTurn)
    }

    @Test
    fun tusinTest() = runTest {
        resetValue(0, 1, 10, 10, 1, 5)
        startPhase()

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.KODAMA_TU_SIN, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KODAMA_TU_SIN, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.KODAMA_TU_SIN, LocationEnum.ADDITIONAL_CARD))
    }

    @Test
    fun spearSoldierTest() = runTest {
        resetValue(0, 1, 10, 10, 3, 5)
        gameStatus.player1.lastTurnReact = true

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.SOLDIER_SPEAR_1, LocationEnum.READY_SOLDIER_ZONE)
        useCard(PlayerEnum.PLAYER1, CardName.SOLDIER_SPEAR_1, LocationEnum.READY_SOLDIER_ZONE)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(true, gameStatus.player1.endTurn)
    }

    @Test
    fun shieldSoldierTest() = runTest {
        resetValue(1, 1, 10, 10, 2, 5)

        addCard(PlayerEnum.PLAYER1, CardName.SOLDIER_SHIELD, LocationEnum.READY_SOLDIER_ZONE)
        addReactData(PlayerEnum.PLAYER1, CardName.SOLDIER_SHIELD, LocationEnum.READY_SOLDIER_ZONE)
        addCard(PlayerEnum.PLAYER2, CardName.KODAMA_TU_SIN, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.KODAMA_TU_SIN, LocationEnum.HAND)

        assertEquals(10, gameStatus.player1.life)
        assertEquals(true, gameStatus.player1.endTurn)
    }

    @Test
    fun horseSoldierTest() = runTest {
        resetValue(2, 2, 10, 10, 4, 5)

        addCard(PlayerEnum.PLAYER1, CardName.SOLDIER_HORSE, LocationEnum.READY_SOLDIER_ZONE)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP, mutableListOf(0, 2)))
        useCard(PlayerEnum.PLAYER1, CardName.SOLDIER_HORSE, LocationEnum.READY_SOLDIER_ZONE)

        gameStatus.endPhase(); startPhase()

        addCard(PlayerEnum.PLAYER1, CardName.MIZUKI_BANG_BYEOG, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER1, CardName.MIZUKI_BANG_BYEOG, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_STAR_NAIL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_STAR_NAIL, LocationEnum.HAND)

        assertEquals(10, gameStatus.player1.life)

        gameStatus.endPhase(); startPhase()

        assertEquals(2, gameStatus.player1.concentration)
    }
}