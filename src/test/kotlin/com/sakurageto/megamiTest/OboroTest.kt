package com.sakurageto.megamiTest

import com.sakurageto.ApplicationTest
import com.sakurageto.card.*
import com.sakurageto.gamelogic.MegamiEnum
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import com.sakurageto.protocol.SakuraBaseData
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class OboroTest: ApplicationTest() {
    private fun addInstallationData(playerEnum: PlayerEnum, cardName: CardName){
        when(playerEnum){
            PlayerEnum.PLAYER1 -> {
                player1Connection.putReceiveData(SakuraBaseData(CommandEnum.DECK_RECONSTRUCT_YES))
                player1Connection.putReceiveData(makeData(
                    playerEnum, CommandEnum.SELECT_CARD_REASON_INSTALLATION, mutableListOf(cardName)))
            }
            PlayerEnum.PLAYER2 -> {
                player2Connection.putReceiveData(SakuraBaseData(CommandEnum.DECK_RECONSTRUCT_YES))
                player2Connection.putReceiveData(makeData(
                    playerEnum, CommandEnum.SELECT_CARD_REASON_INSTALLATION, mutableListOf(cardName)))
            }
        }

    }

    @Test
    fun wireTest() = runTest {
        resetValue(0, 1, 10, 10, 4, 4)

        addCard(PlayerEnum.PLAYER1, CardName.OBORO_WIRE, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_SHOOT, LocationEnum.COVER_CARD)

        addInstallationData(PlayerEnum.PLAYER1, CardName.OBORO_WIRE)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        gameStatus.startPhase()

        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun shadowcaltropTest() = runTest {
        resetValue(0, 1, 10, 10, 2, 4)

        addCard(PlayerEnum.PLAYER1, CardName.OBORO_SHADOWCALTROP, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_SHOOT, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)

        addInstallationData(PlayerEnum.PLAYER1, CardName.OBORO_SHADOWCALTROP)
        player1Connection.putReceiveData(SakuraBaseData(CommandEnum.COVER_CARD_SELECT,
            NUMBER_YURINA_ILSUM + SECOND_PLAYER_START_NUMBER))
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        gameStatus.startPhase()

        assertEquals(9, gameStatus.player2.life)
        assertEquals(1, gameStatus.player2.coverCard.size)
    }

    @Test
    fun zangekiranbuTest() = runTest {
        resetValue(0, 5, 10, 10, 4, 4)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.OBORO_WIRE, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_SHOOT, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_ZANGEKIRANBU, LocationEnum.HAND)

        addInstallationData(PlayerEnum.PLAYER1, CardName.OBORO_WIRE)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        gameStatus.startPhase()

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.OBORO_ZANGEKIRANBU, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun ninjaWalkTest() = runTest {
        resetValue(0, 2, 10, 10, 2, 4)

        addCard(PlayerEnum.PLAYER1, CardName.OBORO_NINJAWALK, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_WIRE, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_ZANGEKIRANBU, LocationEnum.HAND)

        addInstallationData(PlayerEnum.PLAYER1, CardName.OBORO_NINJAWALK)
        player1Connection.putReceiveData(makeData(
            PlayerEnum.PLAYER1, CommandEnum.SELECT_CARD_REASON_INSTALLATION, mutableListOf(CardName.OBORO_WIRE)))
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        gameStatus.startPhase()

        assertEquals(0, gameStatus.player2.aura)
        assertEquals(3, gameStatus.distanceToken)
    }

    @Test
    fun induceOneTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 5)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_INDUCE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.OBORO_INDUCE, LocationEnum.HAND)

        assertEquals(4, gameStatus.distanceToken)
        assertEquals(1, gameStatus.player2.aura)
    }

    @Test
    fun induceTwoTest() = runTest {
        resetValue(0, 1, 10, 10, 5, 5)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_INDUCE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.OBORO_INDUCE, LocationEnum.HAND)

        assertEquals(1, gameStatus.player2.flare)
        assertEquals(0, gameStatus.player2.aura)
    }

    @Test
    fun cloneTest() = runTest {
        resetValue(0, 4, 10, 10, 6, 5)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.OBORO_CLONE, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_RAPIDFIRE, LocationEnum.COVER_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_HIMIKA_RAPIDFIRE)))

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.OBORO_CLONE, LocationEnum.HAND)

        assertEquals(2, gameStatus.player2.aura)
        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun bioactivityTest() = runTest {
        resetValue(0, 0, 10, 10, 6, 0)

        addCard(PlayerEnum.PLAYER1, CardName.OBORO_BIOACTIVITY, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_POBARAM, LocationEnum.YOUR_USED_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.OBORO_BIOACTIVITY, LocationEnum.HAND)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YURINA_POBARAM, LocationEnum.SPECIAL_CARD))
    }

    @Test
    fun kumasukeTest() = runTest {
        resetValue(0, 5, 10, 10, 3, 0)
        gameStatus.player1.fullAction = true; gameStatus.player1.flare = 4

        addCard(PlayerEnum.PLAYER1, CardName.OBORO_WIRE, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_SHADOWCALTROP, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_KUMASUKE, LocationEnum.SPECIAL_CARD)

        for(i in 1..3){
            addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        }
        useCard(PlayerEnum.PLAYER1, CardName.OBORO_KUMASUKE, LocationEnum.SPECIAL_CARD)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(0, gameStatus.player1.flare)
    }

    @Test
    fun tobikageTest() = runTest {
        resetValue(1, 1, 10, 10, 3, 0)
        gameStatus.player2.flare = 4

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.OBORO_NINJAWALK, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.OBORO_WIRE, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.OBORO_TOBIKAGE, LocationEnum.SPECIAL_CARD)

        addReactData(PlayerEnum.PLAYER2, CardName.OBORO_TOBIKAGE, LocationEnum.SPECIAL_CARD)
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_NINJAWALK)))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_INSTALLATION, mutableListOf(
            SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_WIRE)))
        addReactData(PlayerEnum.PLAYER1, CardName.CARD_UNNAME, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
        assertEquals(8, gameStatus.player1.life)
        assertEquals(0, gameStatus.player2.flare)
    }

    @Test
    fun ulooTest() = runTest {
        suspend fun ulooEffectTest(){
            resetValue(1, 1, 10, 10, 3, 3)

            addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
            addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

            addInstallationData(PlayerEnum.PLAYER1, CardName.OBORO_WIRE)
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
                NUMBER_OBORO_NINJAWALK)))
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
            player1Connection.putReceiveData(makeData(
                PlayerEnum.PLAYER1, CommandEnum.SELECT_CARD_REASON_INSTALLATION, mutableListOf(CardName.OBORO_SHADOWCALTROP)))
            startPhase()

            assertEquals(7, gameStatus.player2.life)
            assertEquals(2, gameStatus.distanceToken)
        }

        resetValue(1, 1, 10, 10, 3, 3)
        gameStatus.player1.flare = 3

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP, mutableListOf(0, 3)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
            mutableListOf(NUMBER_OBORO_NINJAWALK, NUMBER_OBORO_WIRE, NUMBER_OBORO_SHADOWCALTROP)
        ))
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_NINJAWALK, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_SHADOWCALTROP, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_WIRE, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_ULOO, LocationEnum.SPECIAL_CARD)

        useCard(PlayerEnum.PLAYER1, CardName.OBORO_ULOO, LocationEnum.SPECIAL_CARD)
        assertEquals(3, gameStatus.player1.coverCard.size)
        ulooEffectTest()
    }

    @Test
    fun mikazraTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 1)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_MIKAZRA, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.OBORO_MIKAZRA, LocationEnum.SPECIAL_CARD)

        assertEquals(1, gameStatus.player1.flare)
        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun shurikenTest() = runTest {
        suspend fun shurikenEffectTest() {
            addCard(PlayerEnum.PLAYER1, CardName.OBORO_WIRE, LocationEnum.COVER_CARD)
            addCard(PlayerEnum.PLAYER1, CardName.OBORO_SHADOWCALTROP, LocationEnum.COVER_CARD)
            addCard(PlayerEnum.PLAYER1, CardName.OBORO_NINJAWALK, LocationEnum.COVER_CARD)
            addCard(PlayerEnum.PLAYER2, CardName.YURINA_GIBACK, LocationEnum.COVER_CARD)
            addCard(PlayerEnum.PLAYER2, CardName.YURINA_BEAN_BULLET, LocationEnum.COVER_CARD)

            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
            gameStatus.endPhase()

            assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.OBORO_SHURIKEN, LocationEnum.HAND))
        }

        resetValue(0, 1, 10, 10, 3, 0)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_SHURIKEN, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.OBORO_SHURIKEN, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        shurikenEffectTest()
    }

    @Test
    fun ambushTest() = runTest {
        resetValue(0, 2, 10, 10, 3, 3)
        gameStatus.player1.fullAction = true

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_GIBACK, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_AMBUSH, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.OBORO_AMBUSH, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun branchOfDivineTest() = runTest {
        resetValue(0, 0,10, 10, 10, 0)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.OBORO_LAST_CRYSTAL, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_BRANCH_OF_DIVINE, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.OBORO_BRANCH_OF_DIVINE, LocationEnum.SPECIAL_CARD)

        assertEquals(1, gameStatus.player1.flare)
        assertEquals(1, gameStatus.player1.aura)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.OBORO_LAST_CRYSTAL, LocationEnum.SPECIAL_CARD))
    }

    @Test
    fun lastCrystalTest() = runTest {
        resetValue(0, 0, 10, 1, 4, 0)
        gameStatus.player2.flare = 3

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER2, CardName.OBORO_LAST_CRYSTAL, LocationEnum.YOUR_USED_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_WIRE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.OBORO_WIRE, LocationEnum.HAND)

        assertEquals(false, gameStatus.gameEnd)
        assertEquals(1, gameStatus.player2.life)
    }

    @Test
    fun hologramKunaiTest() = runTest {
        MegamiEnum.OBORO_A2.settingForAnother(PlayerEnum.PLAYER1, gameStatus)
        resetValue(0, 0, 10, 10, 5, 5)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_OBORO_CUSTOM_PARTS_A)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_HOLOGRAM_KUNAI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.OBORO_HOLOGRAM_KUNAI, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(1, gameStatus.player1.assemblyZone?.size)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.OBORO_HOLOGRAM_KUNAI, LocationEnum.COVER_CARD))
    }

    @Test
    fun gigasukeTest() = runTest {
        gameStatus.player1.assemblyZone = hashMapOf()
        resetValue(0, 5, 10, 10, 4, 5)
        gameStatus.player1.flare = 9

        for(i in 1..4){
            addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        }

        addCard(PlayerEnum.PLAYER1, CardName.OBORO_MAIN_PARTS_X, LocationEnum.ASSEMBLY_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_CUSTOM_PARTS_A, LocationEnum.ASSEMBLY_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_CUSTOM_PARTS_B, LocationEnum.ASSEMBLY_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_CUSTOM_PARTS_C, LocationEnum.ASSEMBLY_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_CUSTOM_PARTS_D, LocationEnum.ASSEMBLY_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_NINJAWALK, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_SHADOWCALTROP, LocationEnum.COVER_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.OBORO_GIGASUKE, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.OBORO_GIGASUKE, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun blurryDocumentTest() = runTest {
        suspend fun usedEffectTest(){
            player1Connection.putReceiveData(makeData(CommandEnum.DECK_RECONSTRUCT_YES))
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NOT))
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
                NUMBER_OBORO_MAIN_PARTS_X)))
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_INSTALLATION, mutableListOf()))
            player1Connection.putReceiveData(makeData(CommandEnum.CHOOSE_AURA))

            gameStatus.startPhase()

            assertEquals(2, gameStatus.player1.assemblyZone?.size)
        }

        MegamiEnum.OBORO_A2.settingForAnother(PlayerEnum.PLAYER1, gameStatus)
        resetValue(0, 0, 10, 10, 5, 5)
        gameStatus.player1.fullAction = true

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_OBORO_CUSTOM_PARTS_A)))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_FORWARD))
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_BLURRY_DOCUMENT_ELECTRICSOUCHI, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_HOLOGRAM_KUNAI, LocationEnum.COVER_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.OBORO_BLURRY_DOCUMENT_ELECTRICSOUCHI, LocationEnum.SPECIAL_CARD)

        assertEquals(1, gameStatus.player1.aura)
        assertEquals(1, gameStatus.player1.assemblyZone?.size)

        usedEffectTest()
    }

    @Test
    fun digitalInstallationTest() = runTest {
        gameStatus.player1.assemblyZone = hashMapOf()
        resetValue(0, 2, 10, 10, 4, 5)

        addCard(PlayerEnum.PLAYER1, CardName.OBORO_MAIN_PARTS_X, LocationEnum.ASSEMBLY_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_CUSTOM_PARTS_A, LocationEnum.ASSEMBLY_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_CUSTOM_PARTS_B, LocationEnum.ASSEMBLY_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_CUSTOM_PARTS_C, LocationEnum.ASSEMBLY_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_CUSTOM_PARTS_D, LocationEnum.ASSEMBLY_YOUR)
        addCard(PlayerEnum.PLAYER2, CardName.OBORO_INDUCE, LocationEnum.HAND)

        player1Connection.putReceiveData(makeData(CommandEnum.DECK_RECONSTRUCT_YES))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_OBORO_CUSTOM_PARTS_A, NUMBER_OBORO_CUSTOM_PARTS_B, NUMBER_OBORO_CUSTOM_PARTS_C,
            NUMBER_OBORO_CUSTOM_PARTS_D
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addReactData(PlayerEnum.PLAYER2, CardName.OBORO_INDUCE, LocationEnum.HAND)

        gameStatus.startPhase()

        assertEquals(5, gameStatus.distanceToken)
        assertEquals(2, gameStatus.player1.concentration)
        assertEquals(7, gameStatus.player2.life)
    }
}