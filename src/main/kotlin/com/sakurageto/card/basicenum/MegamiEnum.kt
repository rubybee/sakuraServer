package com.sakurageto.card.basicenum

import com.sakurageto.card.Card
import com.sakurageto.card.CardName
import com.sakurageto.gamelogic.GameStatus
import com.sakurageto.gamelogic.GameVersion
import com.sakurageto.gamelogic.megamispecial.Stratagem
import com.sakurageto.gamelogic.megamispecial.Umbrella
import com.sakurageto.gamelogic.megamispecial.storyboard.StoryBoard
import com.sakurageto.protocol.LocationEnum

enum class MegamiEnum(var real_number: Int) {
    NONE(0),
    KODAMA(1),
    KIRIKO(2),
    ZANKA(3),
    OUKA(4),
    SAI_TOKO(5),
    YURINA(10),
    YURINA_A1(11),
    YURINA_A2(12),
    SAINE(20),
    SAINE_A1(21),
    SAINE_A2(22),
    HIMIKA(30),
    HIMIKA_A1(31),
    TOKOYO(40),
    TOKOYO_A1(41),
    TOKOYO_A2(42),
    OBORO(50),
    OBORO_A1(51),
    OBORO_A2(52),
    YUKIHI(60),
    YUKIHI_A1(61),
    SHINRA(70),
    SHINRA_A1(71),
    HAGANE(80),
    HAGANE_A1(81),
    CHIKAGE(90),
    CHIKAGE_A1(91),
    KURURU(100),
    KURURU_A1(101),
    KURURU_A2(102),
    THALLYA(110),
    THALLYA_A1(111),
    RAIRA(120),
    RAIRA_A1(121),
    UTSURO(130),
    UTSURO_A1(131),
    HONOKA(140),
    HONOKA_A1(141),
    KORUNU(150),
    YATSUHA(160),
    YATSUHA_A1(161),
    YATSUHA_AA1(162),
    HATSUMI(170),
    HATSUMI_A1(171),
    MIZUKI(180),
    MEGUMI(190),
    KANAWE(200),
    KAMUWI(210),
    RENRI(220),
    RENRI_A1(221),
    AKINA(230),
    SHISUI(240),
    MISORA(250);

    fun equal (megami: MegamiEnum): Boolean{
        if(this == SAI_TOKO){
            when(megami){
                SAINE -> return true
                TOKOYO -> return true
                else -> {}
            }
        }
        else if(megami == SAI_TOKO){
            when(this){
                SAINE -> return true
                TOKOYO -> return true
                else -> {}
            }
        }
        return this == megami
    }

    fun changeNormalMegami(): MegamiEnum {
        val anotherNumber = this.real_number % 10
        return if(anotherNumber == 0){
            this
        } else{
            fromInt(this.real_number - anotherNumber)
        }
    }

    fun getAllNormalCardName(version: GameVersion): List<CardName>{
        return CardName.returnNormalCardNameByMegami(version, this)
    }

    fun getAllAdditionalCardName(): List<CardName>{
        return CardName.returnAdditionalCardNameByMegami(this)
    }

    fun getAllSpecialCardName(version: GameVersion): List<CardName>{
        return CardName.returnSpecialCardNameByMegami(version, this)
    }

    fun settingForOriginal(player: PlayerEnum, game_status: GameStatus) {
        when(this.changeNormalMegami()){
            YUKIHI -> settingForYukihi(player, game_status)
            SHINRA -> settingForShinra(player, game_status)
            CHIKAGE -> settingForChikage(player, game_status)
            THALLYA -> settingForThallya(player, game_status)
            RAIRA -> settingForRaira(player, game_status)
            MIZUKI -> settingForMizuki(player, game_status)
            MEGUMI -> settingForMegumi(player, game_status)
            KANAWE -> settingForKanawe(player, game_status)
            KAMUWI -> settingForKamuwi(player, game_status)
            AKINA -> settingForAkina(player, game_status)
            MISORA -> settingForMisora(player, game_status)
            else -> {}
        }
    }
    
    fun settingForAnother(player: PlayerEnum, game_status: GameStatus) {
        when(this){
            YATSUHA_AA1 -> settingForYatsuhaAA1(player, game_status)
            RENRI_A1 -> settingForRenriA1(player, game_status)
            OBORO_A2 -> settingForOboroA2(player, game_status)
            else -> {}
        }
    }

    private fun settingForYukihi(player: PlayerEnum, game_status: GameStatus){
        val nowPlayer = game_status.getPlayer(player)

        nowPlayer.umbrella = Umbrella.FOLD
        if(nowPlayer.megamiOneNormalForm() == YUKIHI){
            nowPlayer.megamiCard = Card.cardMakerByName(game_status.getPlayer(player).firstTurn, CardName.YUKIHI_YUKIHI, player,
                LocationEnum.YOUR_USED_CARD, game_status.version)
            nowPlayer.megamiCard?.special_card_state = SpecialCardEnum.PLAYED
        }
        else{
            nowPlayer.megamiCard2 = Card.cardMakerByName(game_status.getPlayer(player).firstTurn, CardName.YUKIHI_YUKIHI, player,
                LocationEnum.YOUR_USED_CARD, game_status.version)
            nowPlayer.megamiCard2?.special_card_state = SpecialCardEnum.PLAYED
        }
    }

    private fun settingForShinra(player: PlayerEnum, game_status: GameStatus){
        val nowPlayer = game_status.getPlayer(player)

        nowPlayer.stratagem = Stratagem.SHIN_SAN
        if(nowPlayer.megamiOneNormalForm() == SHINRA){
            nowPlayer.megamiCard = Card.cardMakerByName(game_status.getPlayer(player).firstTurn, CardName.SHINRA_SHINRA, player,
                LocationEnum.YOUR_USED_CARD, game_status.version)
            nowPlayer.megamiCard?.special_card_state = SpecialCardEnum.PLAYED
        }
        else{
            nowPlayer.megamiCard2 = Card.cardMakerByName(game_status.getPlayer(player).firstTurn, CardName.SHINRA_SHINRA, player,
                LocationEnum.YOUR_USED_CARD, game_status.version)
            nowPlayer.megamiCard2?.special_card_state = SpecialCardEnum.PLAYED
        }
    }

    private fun settingForChikage(player: PlayerEnum, game_status: GameStatus){
        val nowPlayer = game_status.getPlayer(player)
        val turnCheck = game_status.getPlayer(player.opposite()).firstTurn

        for(card_name in CardName.poisonList){
            val card = Card.cardMakerByName(turnCheck, card_name, player.opposite(),
                LocationEnum.POISON_BAG, game_status.version)
            nowPlayer.poisonBag[card.card_data.card_name] = card
        }
    }

    private fun settingForThallya(player: PlayerEnum, game_status: GameStatus){
        when(player){
            PlayerEnum.PLAYER1 -> {
                game_status.player1.artificialToken = 5
                game_status.player1ManeuverListener = ArrayDeque()
            }
            PlayerEnum.PLAYER2 -> {
                game_status.player2.artificialToken = 5
                game_status.player2ManeuverListener = ArrayDeque()
            }
        }
    }

    private fun settingForRaira(player: PlayerEnum, game_status: GameStatus){
        val nowPlayer = game_status.getPlayer(player)

        nowPlayer.windGauge = 0
        nowPlayer.thunderGauge = 0
    }

    private fun settingForMizuki(player: PlayerEnum, game_status: GameStatus){
        val nowPlayer = game_status.getPlayer(player)
        val turnCheck = game_status.getPlayer(player).firstTurn

        for(card_name in CardName.soldierList){
            val card = Card.cardMakerByName(turnCheck, card_name, player,
                LocationEnum.NOT_READY_SOLDIER_ZONE, game_status.version)
            nowPlayer.notReadySoldierZone[card.card_number] = card
        }
    }

    private fun settingForMegumi(player: PlayerEnum, game_status: GameStatus){
        val nowPlayer = game_status.getPlayer(player)
        nowPlayer.notReadySeed = 5
    }

    private fun settingForKanawe(player: PlayerEnum, game_status: GameStatus){
        val nowPlayer = game_status.getPlayer(player)

        nowPlayer.nowAct = StoryBoard.getActByNumber(0)
        if(nowPlayer.megamiOneNormalForm() == KANAWE){
            nowPlayer.megamiCard = Card.cardMakerByName(game_status.getPlayer(player).firstTurn,
                CardName.KANAWE_KANAWE, player, LocationEnum.YOUR_USED_CARD, game_status.version)
            nowPlayer.megamiCard?.special_card_state = SpecialCardEnum.PLAYED
        }
        else{
            nowPlayer.megamiCard2 = Card.cardMakerByName(game_status.getPlayer(player).firstTurn,
                CardName.KANAWE_KANAWE, player, LocationEnum.YOUR_USED_CARD, game_status.version)
            nowPlayer.megamiCard2?.special_card_state = SpecialCardEnum.PLAYED
        }
    }

    private fun settingForKamuwi(player: PlayerEnum, game_status: GameStatus){
        val nowPlayer = game_status.getPlayer(player)

        nowPlayer.tabooGauge = 0
    }

    private fun settingForAkina(player: PlayerEnum, game_status: GameStatus){
        val nowPlayer = game_status.getPlayer(player)

        nowPlayer.flow = 0
        if(nowPlayer.megamiOneNormalForm() == AKINA){
            nowPlayer.megamiCard = Card.cardMakerByName(game_status.getPlayer(player).firstTurn, CardName.AKINA_AKINA, player,
                LocationEnum.YOUR_USED_CARD, game_status.version)
            nowPlayer.megamiCard?.special_card_state = SpecialCardEnum.PLAYED
        }
        else{
            nowPlayer.megamiCard2 = Card.cardMakerByName(game_status.getPlayer(player).firstTurn, CardName.AKINA_AKINA, player,
                LocationEnum.YOUR_USED_CARD, game_status.version)
            nowPlayer.megamiCard2?.special_card_state = SpecialCardEnum.PLAYED
        }

        nowPlayer.marketPrice = 2
    }

    private fun settingForMisora(player: PlayerEnum, game_status: GameStatus){
        val nowPlayer = game_status.getPlayer(player)

        if(nowPlayer.megamiOneNormalForm() == MISORA){
            nowPlayer.megamiCard = Card.cardMakerByName(game_status.getPlayer(player).firstTurn, CardName.MISORA_MISORA, player,
                LocationEnum.YOUR_USED_CARD, game_status.version)
            nowPlayer.megamiCard?.special_card_state = SpecialCardEnum.PLAYED
        }
        else{
            nowPlayer.megamiCard2 = Card.cardMakerByName(game_status.getPlayer(player).firstTurn, CardName.MISORA_MISORA, player,
                LocationEnum.YOUR_USED_CARD, game_status.version)
            nowPlayer.megamiCard2?.special_card_state = SpecialCardEnum.PLAYED
        }
    }

    private fun settingForRenriA1(player: PlayerEnum, game_status: GameStatus){
        val nowPlayer = game_status.getPlayer(player)
        nowPlayer.relic = HashMap()
        nowPlayer.perjuryInstallation = hashSetOf(CardName.RENRI_FALSE_WEAPON)
        for(card_name in CardName.relicList){
            val card = Card.cardMakerByName(nowPlayer.firstTurn, card_name, player,
                LocationEnum.RELIC_YOUR, game_status.version)
            nowPlayer.relic!![card.card_number] = card
        }
    }

    private fun settingForYatsuhaAA1(player: PlayerEnum, game_status: GameStatus){
        val nowPlayer = game_status.getPlayer(player)

        nowPlayer.memory = hashMapOf()
    }

    private fun settingForOboroA2(player: PlayerEnum, game_status: GameStatus){
        val nowPlayer = game_status.getPlayer(player)

        nowPlayer.assemblyZone = hashMapOf()
        nowPlayer.unassemblyZone = hashMapOf()

        for(card_name in CardName.partsList){
            val card = Card.cardMakerByName(nowPlayer.firstTurn, card_name, player,
                LocationEnum.UNASSEMBLY_YOUR, game_status.version)
            nowPlayer.unassemblyZone!![card.card_number] = card
        }
    }

    companion object {
        const val NUMBER_RENRI_ORIGIN_NUMBER = 22

        fun fromInt(value: Int) = MegamiEnum.values().first { it.real_number == value }
    }
}