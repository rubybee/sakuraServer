package com.sakurageto.megamiTest

import com.sakurageto.ApplicationTest
import com.sakurageto.card.*
import com.sakurageto.card.basicenum.CardClass
import com.sakurageto.card.basicenum.CardType
import com.sakurageto.card.basicenum.PlayerEnum
import com.sakurageto.card.basicenum.SubType
import com.sakurageto.gamelogic.GameVersion
import com.sakurageto.card.basicenum.MegamiEnum
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ThallyaTest: ApplicationTest() {
    @Before
    fun setting(){
        gameStatus.player1.megamiOne = MegamiEnum.THALLYA
        MegamiEnum.THALLYA.settingForOriginal(PlayerEnum.PLAYER1, gameStatus)

        gameStatus.player1.additionalHand[CardName.FORM_YAKSHA] =
            Card.cardMakerByName(true, CardName.FORM_YAKSHA, PlayerEnum.PLAYER1, LocationEnum.ADDITIONAL_CARD, GameVersion.VERSION_9_1)
        gameStatus.player1.additionalHand[CardName.FORM_NAGA] =
            Card.cardMakerByName(true, CardName.FORM_NAGA, PlayerEnum.PLAYER1, LocationEnum.ADDITIONAL_CARD, GameVersion.VERSION_9_1)
        gameStatus.player1.additionalHand[CardName.FORM_GARUDA] =
            Card.cardMakerByName(true, CardName.FORM_GARUDA, PlayerEnum.PLAYER1, LocationEnum.ADDITIONAL_CARD, GameVersion.VERSION_9_1)
        gameStatus.player1.additionalHand[CardName.FORM_KINNARI] =
            Card.cardMakerByName(true, CardName.FORM_KINNARI, PlayerEnum.PLAYER1, LocationEnum.ADDITIONAL_CARD, GameVersion.VERSION_9_1)
        gameStatus.player1.additionalHand[CardName.FORM_DEVA] =
            Card.cardMakerByName(true, CardName.FORM_DEVA, PlayerEnum.PLAYER1, LocationEnum.ADDITIONAL_CARD, GameVersion.VERSION_9_1)
        gameStatus.player1.additionalHand[CardName.FORM_ASURA] =
            Card.cardMakerByName(true, CardName.FORM_ASURA, PlayerEnum.PLAYER1, LocationEnum.ADDITIONAL_CARD, GameVersion.VERSION_9_1)
    }

    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.THALLYA_BURNING_STEAM, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.THALLYA_WAVING_EDGE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.THALLYA_SHIELD_CHARGE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.THALLYA_STEAM_CANNON, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.THALLYA_STUNT, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.THALLYA_ROARING, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.THALLYA_TURBO_SWITCH, CardClass.NORMAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.THALLYA_ALPHA_EDGE, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.THALLYA_OMEGA_BURST, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.THALLYA_THALLYA_MASTERPIECE, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.THALLYA_JULIA_BLACKBOX, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.FULL_POWER)

        cardTypeTest(CardName.THALLYA_QUICK_CHANGE, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.THALLYA_BLACKBOX_NEO, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.THALLYA_OMNIS_BLASTER, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
    }

    @Test
    fun burningSteamTest() = runTest {
        resetValue(0, 1, 10, 10, 5, 4)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_BURNING_STEAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_BURNING_STEAM, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(4, gameStatus.getAdjustDistance())

        startPhase()

        assertEquals(5, gameStatus.getAdjustDistance())
    }

    @Test
    fun wavingEdgeTest() = runTest {
        resetValue(0, 2, 10, 10, 3, 4)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_WAVING_EDGE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_WAVING_EDGE, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(4, gameStatus.getAdjustDistance())
    }

    @Test
    fun shieldChargeAuraTest() = runTest {
        resetValue(0, 3, 10, 10, 1, 4)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_SHIELD_CHARGE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_SHIELD_CHARGE, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.aura)
        assertEquals(4, gameStatus.getAdjustDistance())
    }

    @Test
    fun shieldChargeLifeTest() = runTest {
        resetValue(0, 2, 10, 10, 1, 4)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_SHIELD_CHARGE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_SHIELD_CHARGE, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(3, gameStatus.getAdjustDistance())
    }

    @Test
    fun steamCanonTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 4)

        gameStatus.combust(PlayerEnum.PLAYER1, 1)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_STEAM_CANNON, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_STEAM_CANNON, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(5, gameStatus.player1.artificialToken)
    }

    @Test
    fun steamCanonFullPowerTest() = runTest {
        resetValue(0, 2, 10, 10, 2, 4)
        gameStatus.player1.fullAction = true

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_STEAM_CANNON, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_STEAM_CANNON, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun stuntTest() = runTest {
        resetValue(2, 0, 10, 10, 0, 0)

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_STUNT, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_STUNT, LocationEnum.HAND)

        assertEquals(true, gameStatus.player2.shrink)
        assertEquals(2, gameStatus.player1.flare)
    }

    @Test
    fun roaringTest() = runTest {
        resetValue(0, 0, 10, 10, 0, 0)
        gameStatus.player1.concentration = 1; gameStatus.player2.concentration = 1
        gameStatus.combust(PlayerEnum.PLAYER1, 1)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_ROARING, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_ROARING, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.concentration)
        assertEquals(0, gameStatus.player1.concentration)
        assertEquals(true, gameStatus.player2.shrink)
        assertEquals(5, gameStatus.player1.artificialToken)
    }

    @Test
    fun alphaEdgeTest() = runTest {
        suspend fun returnTest() {
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

            addCard(PlayerEnum.PLAYER1, CardName.THALLYA_TURBO_SWITCH, LocationEnum.HAND)
            useCard(PlayerEnum.PLAYER1, CardName.THALLYA_TURBO_SWITCH, LocationEnum.HAND)

            assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.THALLYA_ALPHA_EDGE, LocationEnum.SPECIAL_CARD))
        }

        resetValue(0, 0, 10, 10, 5, 0)
        gameStatus.player1.flare = 1

        addReactData(PlayerEnum.PLAYER2)

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_ALPHA_EDGE, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_ALPHA_EDGE, LocationEnum.SPECIAL_CARD)

        assertEquals(9, gameStatus.player2.life)

        returnTest()
    }

    @Test
    fun omegaBurstTest() = runTest {
        resetValue(0, 2, 10, 10, 2, 4)
        gameStatus.player1.fullAction = true; gameStatus.player2.flare = 4

        gameStatus.player2.artificialTokenBurn = 5; gameStatus.player2.artificialToken = 0
        addCard(PlayerEnum.PLAYER2, CardName.THALLYA_OMEGA_BURST, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.THALLYA_OMEGA_BURST, LocationEnum.SPECIAL_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_STEAM_CANNON, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_STEAM_CANNON, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
        assertEquals(5, gameStatus.player2.artificialToken)
    }

    @Test
    fun thallyaMasterpieceTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 0)
        gameStatus.player1.flare = 1

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_THALLYA_MASTERPIECE, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_THALLYA_MASTERPIECE, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_TURBO_SWITCH, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_TURBO_SWITCH, LocationEnum.HAND)

        assertEquals(3, gameStatus.getAdjustDistance())
    }

    @Test
    fun juliaBoxTest() = runTest {
        suspend fun yakshaTest() {
            resetValue(0, 1, 10, 10, 4, 0)
            gameStatus.player1.concentration = 1

            addReactData(PlayerEnum.PLAYER2)
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

            gameStatus.doBasicOperation(PlayerEnum.PLAYER1, CommandEnum.ACTION_YAKSHA, -1)

            assertEquals(9, gameStatus.player2.life)
            assertEquals(3, gameStatus.getAdjustDistance())
        }

        resetValue(0, 0, 10, 10, 5, 0)
        gameStatus.player1.artificialToken = 0; gameStatus.player1.artificialTokenBurn = 5
        gameStatus.player1.fullAction = true; gameStatus.player1.flare = 2

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_FORM_YAKSHA
        )))

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_JULIA_BLACKBOX, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_JULIA_BLACKBOX, LocationEnum.SPECIAL_CARD)

        assertEquals(2, gameStatus.player1.artificialToken)
        assertEquals(0, gameStatus.player1.flare)

        yakshaTest()
    }

    @Test
    fun nagaTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 0)
        gameStatus.player1.artificialToken = 0; gameStatus.player1.artificialTokenBurn = 5
        gameStatus.player1.fullAction = true; gameStatus.player1.flare = 2; gameStatus.player2.flare = 5

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_FORM_NAGA
        )))

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_JULIA_BLACKBOX, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_JULIA_BLACKBOX, LocationEnum.SPECIAL_CARD)

        assertEquals(2, gameStatus.player2.flare)

        addCard(PlayerEnum.PLAYER2, CardName.THALLYA_ROARING, LocationEnum.DECK)

        gameStatus.doBasicOperation(PlayerEnum.PLAYER1, CommandEnum.ACTION_NAGA, -1)

        assertEquals(1, gameStatus.player2.discard.size)
        assertEquals(0, gameStatus.player2.normalCardDeck.size)
    }

    @Test
    fun garudaTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 1)
        gameStatus.player1.artificialToken = 0; gameStatus.player1.artificialTokenBurn = 5
        gameStatus.player1.fullAction = true; gameStatus.player1.flare = 2; gameStatus.player2.flare = 5

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_FORM_GARUDA
        )))

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_SHIELD_CHARGE, LocationEnum.DECK)
        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_STEAM_CANNON, LocationEnum.DECK)
        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_STUNT, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_BURNING_STEAM, LocationEnum.HAND)


        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_JULIA_BLACKBOX, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_JULIA_BLACKBOX, LocationEnum.SPECIAL_CARD)

        gameStatus.doBasicOperation(PlayerEnum.PLAYER1, CommandEnum.ACTION_GARUDA, -1)

        assertEquals(6, gameStatus.distanceToken)
        assertEquals(4, gameStatus.player1.hand.size)

        gameStatus.endPhase()

        assertEquals(4, gameStatus.player1.hand.size)
    }

    @Test
    fun quickChangeTest() = runTest {
        resetValue(0, 2, 10, 10, 5, 1)
        gameStatus.combust(PlayerEnum.PLAYER1, 1)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_FORM_ASURA
        )))

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_QUICK_CHANGE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_QUICK_CHANGE, LocationEnum.HAND)

        assertEquals(5, gameStatus.player1.artificialToken)

        addReactData(PlayerEnum.PLAYER2)
        gameStatus.doBasicOperation(PlayerEnum.PLAYER1, CommandEnum.ACTION_ASURA, -1)

        assertEquals(8, gameStatus.player2.life)

        addReactData(PlayerEnum.PLAYER2)
        gameStatus.doBasicOperation(PlayerEnum.PLAYER1, CommandEnum.ACTION_ASURA, -1)

        assertEquals(8, gameStatus.player2.life)
    }


    @Test
    fun blackBoxNeoTest() = runTest {
        suspend fun kinnariTest() {
            resetValue(0, 1, 10, 10, 4, 1)

            gameStatus.endPhase(); startPhase()

            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
            addReactData(PlayerEnum.PLAYER2)
            gameStatus.deckReconstruct(PlayerEnum.PLAYER2, false)

            assertEquals(8, gameStatus.player2.life)

            gameStatus.endPhase(); startPhase()
        }

        suspend fun asuraTest() {
            resetValue(0, 1, 10, 10, 5, 1)
            gameStatus.player1.fullAction = true; gameStatus.player1.flare = 2
            gameStatus.combust(PlayerEnum.PLAYER1, 5)

            addCard(PlayerEnum.PLAYER2, CardName.THALLYA_STUNT, LocationEnum.COVER_CARD)
            addCard(PlayerEnum.PLAYER2, CardName.THALLYA_STEAM_CANNON, LocationEnum.COVER_CARD)
            player2Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
                NUMBER_THALLYA_STUNT + SECOND_PLAYER_START_NUMBER
            )))

            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
                NUMBER_FORM_ASURA
            )))

            addCard(PlayerEnum.PLAYER1, CardName.THALLYA_JULIA_BLACKBOX, LocationEnum.SPECIAL_CARD)
            useCard(PlayerEnum.PLAYER1, CardName.THALLYA_JULIA_BLACKBOX, LocationEnum.SPECIAL_CARD)

            assertEquals(0, gameStatus.player2.coverCard.size)
        }

        suspend fun omnisBlasterTest() {
            resetValue(0, 1, 10, 10, 5, 1)
            gameStatus.player1.flare = 2

            addReactData(PlayerEnum.PLAYER2)

            addCard(PlayerEnum.PLAYER1, CardName.THALLYA_OMNIS_BLASTER, LocationEnum.SPECIAL_CARD)
            useCard(PlayerEnum.PLAYER1, CardName.THALLYA_OMNIS_BLASTER, LocationEnum.SPECIAL_CARD)

            assertEquals(0, gameStatus.player1.flare)
            assertEquals(8, gameStatus.player2.life)
        }

        resetValue(0, 2, 10, 10, 5, 1)
        gameStatus.player1.flare = 2

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_BLACKBOX_NEO, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_BLACKBOX_NEO, LocationEnum.SPECIAL_CARD)

        gameStatus.endPhase(); startPhase()
        gameStatus.endPhase(); startPhase()

        gameStatus.combust(PlayerEnum.PLAYER1, 2)

        gameStatus.endPhase()

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.THALLYA_BLACKBOX_NEO, LocationEnum.SPECIAL_CARD))

        gameStatus.restoreArtificialToken(PlayerEnum.PLAYER1, 2)

        addCard(PlayerEnum.PLAYER2, CardName.THALLYA_STUNT, LocationEnum.DECK)
        addCard(PlayerEnum.PLAYER2, CardName.THALLYA_STEAM_CANNON, LocationEnum.DECK)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_FORM_KINNARI
        )))
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_BLACKBOX_NEO, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player2.normalCardDeck.size)

        kinnariTest()
        asuraTest()
        omnisBlasterTest()
    }

    @Test
    fun devaTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 2)
        gameStatus.player1.artificialToken = 0; gameStatus.player1.artificialTokenBurn = 5
        gameStatus.player1.fullAction = true; gameStatus.player1.flare = 2

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_FORM_DEVA
        )))

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_JULIA_BLACKBOX, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_JULIA_BLACKBOX, LocationEnum.SPECIAL_CARD)

        assertEquals(4, gameStatus.player1.artificialToken)
        assertEquals(2, gameStatus.player1.aura)

        gameStatus.endPhase(); startPhase()

        addCard(PlayerEnum.PLAYER2, CardName.THALLYA_STUNT, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER2, CardName.THALLYA_SHIELD_CHARGE, LocationEnum.DISCARD_YOUR)

        assertEquals(1, gameStatus.player1.concentration)
    }


}