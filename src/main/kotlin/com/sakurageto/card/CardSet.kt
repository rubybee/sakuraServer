package com.sakurageto.card

import com.sakurageto.gamelogic.*
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum

object CardSet {
    val cardNameHashmapFirst = HashMap<CardName, Int>()
    val cardNameHashmapSecond = HashMap<CardName, Int>()
    val cardNumberHashmap = HashMap<Int, CardName>()

    private fun hashMapInit(){
        //for first turn player 0~9999
        cardNameHashmapFirst[CardName.YURINA_CHAM] = 100
        cardNameHashmapFirst[CardName.YURINA_ILSUM] = 101
        cardNameHashmapFirst[CardName.YURINA_JARUCHIGI] = 102
        cardNameHashmapFirst[CardName.YURINA_GUHAB] = 103
        cardNameHashmapFirst[CardName.YURINA_GIBACK] = 104
        cardNameHashmapFirst[CardName.YURINA_APDO] = 105
        cardNameHashmapFirst[CardName.YURINA_GIYENBANJO] = 106
        cardNameHashmapFirst[CardName.YURINA_WOLYUNGNACK] = 107
        cardNameHashmapFirst[CardName.YURINA_POBARAM] = 108
        cardNameHashmapFirst[CardName.YURINA_JJOCKBAE] = 109
        cardNameHashmapFirst[CardName.YURINA_JURUCK] = 110

        cardNameHashmapFirst[CardName.SAINE_DOUBLEBEGI] = 200
        cardNameHashmapFirst[CardName.SAINE_HURUBEGI] = 201
        cardNameHashmapFirst[CardName.SAINE_MOOGECHOO] = 202
        cardNameHashmapFirst[CardName.SAINE_GANPA] = 203
        cardNameHashmapFirst[CardName.SAINE_GWONYUCK] = 204
        cardNameHashmapFirst[CardName.SAINE_CHOONGEMJUNG] = 205
        cardNameHashmapFirst[CardName.SAINE_MOOEMBUCK] = 206
        cardNameHashmapFirst[CardName.SAINE_YULDONGHOGEK] = 207
        cardNameHashmapFirst[CardName.SAINE_HANGMUNGGONGJIN] = 208
        cardNameHashmapFirst[CardName.SAINE_EMMOOSHOEBING] = 209
        cardNameHashmapFirst[CardName.SAINE_JONGGEK] = 210

        cardNameHashmapFirst[CardName.HIMIKA_SHOOT] = 300
        cardNameHashmapFirst[CardName.HIMIKA_RAPIDFIRE] = 301
        cardNameHashmapFirst[CardName.HIMIKA_MAGNUMCANON] = 302
        cardNameHashmapFirst[CardName.HIMIKA_FULLBURST] = 303
        cardNameHashmapFirst[CardName.HIMIKA_BACKSTEP] = 304
        cardNameHashmapFirst[CardName.HIMIKA_BACKDRAFT] = 305
        cardNameHashmapFirst[CardName.HIMIKA_SMOKE] = 306
        cardNameHashmapFirst[CardName.HIMIKA_REDBULLET] = 307
        cardNameHashmapFirst[CardName.HIMIKA_CRIMSONZERO] = 308
        cardNameHashmapFirst[CardName.HIMIKA_SCARLETIMAGINE] = 309
        cardNameHashmapFirst[CardName.HIMIKA_BURMILIONFIELD] = 310

        cardNameHashmapFirst[CardName.TOKOYO_BITSUNERIGI] = 400
        cardNameHashmapFirst[CardName.TOKOYO_WOOAHHANTAGUCK] = 401
        cardNameHashmapFirst[CardName.TOKOYO_RUNNINGRABIT] = 402
        cardNameHashmapFirst[CardName.TOKOYO_POETDANCE] = 403
        cardNameHashmapFirst[CardName.TOKOYO_FLIPFAN] = 404
        cardNameHashmapFirst[CardName.TOKOYO_WINDSTAGE] = 405
        cardNameHashmapFirst[CardName.TOKOYO_SUNSTAGE] = 406
        cardNameHashmapFirst[CardName.TOKOYO_KUON] = 407
        cardNameHashmapFirst[CardName.TOKOYO_THOUSANDBIRD] = 408
        cardNameHashmapFirst[CardName.TOKOYO_ENDLESSWIND] = 409
        cardNameHashmapFirst[CardName.TOKOYO_TOKOYOMOON] = 410

        cardNameHashmapFirst[CardName.OBORO_WIRE] = 500
        cardNameHashmapFirst[CardName.OBORO_SHADOWCALTROP] = 501
        cardNameHashmapFirst[CardName.OBORO_ZANGEKIRANBU] = 502
        cardNameHashmapFirst[CardName.OBORO_NINJAWALK] = 503
        cardNameHashmapFirst[CardName.OBORO_INDUCE] = 504
        cardNameHashmapFirst[CardName.OBORO_CLONE] = 505
        cardNameHashmapFirst[CardName.OBORO_BIOACTIVITY] = 506
        cardNameHashmapFirst[CardName.OBORO_KUMASUKE] = 507
        cardNameHashmapFirst[CardName.OBORO_TOBIKAGE] = 508
        cardNameHashmapFirst[CardName.OBORO_ULOO] = 509
        cardNameHashmapFirst[CardName.OBORO_MIKAZRA] = 510

        cardNameHashmapFirst[CardName.YUKIHI_YUKIHI] = 100000
        cardNameHashmapFirst[CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE] = 600
        cardNameHashmapFirst[CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS] = 601
        cardNameHashmapFirst[CardName.YUKIHI_PUSH_OUT_SLASH_PULL] = 602
        cardNameHashmapFirst[CardName.YUKIHI_SWING_SLASH_STAB] = 603
        cardNameHashmapFirst[CardName.YUKIHI_TURN_UMBRELLA] = 604
        cardNameHashmapFirst[CardName.YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN] = 605
        cardNameHashmapFirst[CardName.YUKIHI_MAKE_CONNECTION] = 606
        cardNameHashmapFirst[CardName.YUKIHI_FLUTTERING_SNOWFLAKE] = 607
        cardNameHashmapFirst[CardName.YUKIHI_SWAYING_LAMPLIGHT] = 608
        cardNameHashmapFirst[CardName.YUKIHI_CLINGY_MIND] = 609
        cardNameHashmapFirst[CardName.YUKIHI_SWIRLING_GESTURE] = 610



        //for second turn player 10000~19999
        cardNameHashmapSecond[CardName.YURINA_CHAM] = 10100
        cardNameHashmapSecond[CardName.YURINA_ILSUM] = 10101
        cardNameHashmapSecond[CardName.YURINA_JARUCHIGI] = 10102
        cardNameHashmapSecond[CardName.YURINA_GUHAB] = 10103
        cardNameHashmapSecond[CardName.YURINA_GIBACK] = 10104
        cardNameHashmapSecond[CardName.YURINA_APDO] = 10105
        cardNameHashmapSecond[CardName.YURINA_GIYENBANJO] = 10106
        cardNameHashmapSecond[CardName.YURINA_WOLYUNGNACK] = 10107
        cardNameHashmapSecond[CardName.YURINA_POBARAM] = 10108
        cardNameHashmapSecond[CardName.YURINA_JJOCKBAE] = 10109
        cardNameHashmapSecond[CardName.YURINA_JURUCK] = 10110

        cardNameHashmapSecond[CardName.SAINE_DOUBLEBEGI] = 10200
        cardNameHashmapSecond[CardName.SAINE_HURUBEGI] = 10201
        cardNameHashmapSecond[CardName.SAINE_MOOGECHOO] = 10202
        cardNameHashmapSecond[CardName.SAINE_GANPA] = 10203
        cardNameHashmapSecond[CardName.SAINE_GWONYUCK] = 10204
        cardNameHashmapSecond[CardName.SAINE_CHOONGEMJUNG] = 10205
        cardNameHashmapSecond[CardName.SAINE_MOOEMBUCK] = 10206
        cardNameHashmapSecond[CardName.SAINE_YULDONGHOGEK] = 10207
        cardNameHashmapSecond[CardName.SAINE_HANGMUNGGONGJIN] = 10208
        cardNameHashmapSecond[CardName.SAINE_EMMOOSHOEBING] = 10209
        cardNameHashmapSecond[CardName.SAINE_JONGGEK] = 10210

        cardNameHashmapSecond[CardName.HIMIKA_SHOOT] = 10300
        cardNameHashmapSecond[CardName.HIMIKA_RAPIDFIRE] = 10301
        cardNameHashmapSecond[CardName.HIMIKA_MAGNUMCANON] = 10302
        cardNameHashmapSecond[CardName.HIMIKA_FULLBURST] = 10303
        cardNameHashmapSecond[CardName.HIMIKA_BACKSTEP] = 10304
        cardNameHashmapSecond[CardName.HIMIKA_BACKDRAFT] = 10305
        cardNameHashmapSecond[CardName.HIMIKA_SMOKE] = 10306
        cardNameHashmapSecond[CardName.HIMIKA_REDBULLET] = 10307
        cardNameHashmapSecond[CardName.HIMIKA_CRIMSONZERO] = 10308
        cardNameHashmapSecond[CardName.HIMIKA_SCARLETIMAGINE] = 10309
        cardNameHashmapSecond[CardName.HIMIKA_BURMILIONFIELD] = 10310

        cardNameHashmapSecond[CardName.TOKOYO_BITSUNERIGI] = 10400
        cardNameHashmapSecond[CardName.TOKOYO_WOOAHHANTAGUCK] = 10401
        cardNameHashmapSecond[CardName.TOKOYO_RUNNINGRABIT] = 10402
        cardNameHashmapSecond[CardName.TOKOYO_POETDANCE] = 10403
        cardNameHashmapSecond[CardName.TOKOYO_FLIPFAN] = 10404
        cardNameHashmapSecond[CardName.TOKOYO_WINDSTAGE] = 10405
        cardNameHashmapSecond[CardName.TOKOYO_SUNSTAGE] = 10406
        cardNameHashmapSecond[CardName.TOKOYO_KUON] = 10407
        cardNameHashmapSecond[CardName.TOKOYO_THOUSANDBIRD] = 10408
        cardNameHashmapSecond[CardName.TOKOYO_ENDLESSWIND] = 10409
        cardNameHashmapSecond[CardName.TOKOYO_TOKOYOMOON] = 10410

        cardNameHashmapSecond[CardName.OBORO_WIRE] = 10500
        cardNameHashmapSecond[CardName.OBORO_SHADOWCALTROP] = 10501
        cardNameHashmapSecond[CardName.OBORO_ZANGEKIRANBU] = 10502
        cardNameHashmapSecond[CardName.OBORO_NINJAWALK] = 10503
        cardNameHashmapSecond[CardName.OBORO_INDUCE] = 10504
        cardNameHashmapSecond[CardName.OBORO_CLONE] = 10505
        cardNameHashmapSecond[CardName.OBORO_BIOACTIVITY] = 10506
        cardNameHashmapSecond[CardName.OBORO_KUMASUKE] = 10507
        cardNameHashmapSecond[CardName.OBORO_TOBIKAGE] = 10508
        cardNameHashmapSecond[CardName.OBORO_ULOO] = 10509
        cardNameHashmapSecond[CardName.OBORO_MIKAZRA] = 10510

        cardNameHashmapSecond[CardName.YUKIHI_YUKIHI] = 200000
        cardNameHashmapSecond[CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE] = 10600
        cardNameHashmapSecond[CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS] = 10601
        cardNameHashmapSecond[CardName.YUKIHI_PUSH_OUT_SLASH_PULL] = 10602
        cardNameHashmapSecond[CardName.YUKIHI_SWING_SLASH_STAB] = 10603
        cardNameHashmapSecond[CardName.YUKIHI_TURN_UMBRELLA] = 10604
        cardNameHashmapSecond[CardName.YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN] = 10605
        cardNameHashmapSecond[CardName.YUKIHI_MAKE_CONNECTION] = 10606
        cardNameHashmapSecond[CardName.YUKIHI_FLUTTERING_SNOWFLAKE] = 10607
        cardNameHashmapSecond[CardName.YUKIHI_SWAYING_LAMPLIGHT] = 10608
        cardNameHashmapSecond[CardName.YUKIHI_CLINGY_MIND] = 10609
        cardNameHashmapSecond[CardName.YUKIHI_SWIRLING_GESTURE] = 10610



        cardNumberHashmap[100] = CardName.YURINA_CHAM
        cardNumberHashmap[101] = CardName.YURINA_ILSUM
        cardNumberHashmap[102] = CardName.YURINA_JARUCHIGI
        cardNumberHashmap[103] = CardName.YURINA_GUHAB
        cardNumberHashmap[104] = CardName.YURINA_GIBACK
        cardNumberHashmap[105] = CardName.YURINA_APDO
        cardNumberHashmap[106] = CardName.YURINA_GIYENBANJO
        cardNumberHashmap[107] = CardName.YURINA_WOLYUNGNACK
        cardNumberHashmap[108] = CardName.YURINA_POBARAM
        cardNumberHashmap[109] = CardName.YURINA_JJOCKBAE
        cardNumberHashmap[110] = CardName.YURINA_JURUCK

        cardNumberHashmap[200] = CardName.SAINE_DOUBLEBEGI
        cardNumberHashmap[201] = CardName.SAINE_HURUBEGI
        cardNumberHashmap[202] = CardName.SAINE_MOOGECHOO
        cardNumberHashmap[203] = CardName.SAINE_GANPA
        cardNumberHashmap[204] = CardName.SAINE_GWONYUCK
        cardNumberHashmap[205] = CardName.SAINE_CHOONGEMJUNG
        cardNumberHashmap[206] = CardName.SAINE_MOOEMBUCK
        cardNumberHashmap[207] = CardName.SAINE_YULDONGHOGEK
        cardNumberHashmap[208] = CardName.SAINE_HANGMUNGGONGJIN
        cardNumberHashmap[209] = CardName.SAINE_EMMOOSHOEBING
        cardNumberHashmap[210] = CardName.SAINE_JONGGEK

        cardNumberHashmap[300] = CardName.HIMIKA_SHOOT
        cardNumberHashmap[301] = CardName.HIMIKA_RAPIDFIRE
        cardNumberHashmap[302] = CardName.HIMIKA_MAGNUMCANON
        cardNumberHashmap[303] = CardName.HIMIKA_FULLBURST
        cardNumberHashmap[304] = CardName.HIMIKA_BACKSTEP
        cardNumberHashmap[305] = CardName.HIMIKA_BACKDRAFT
        cardNumberHashmap[306] = CardName.HIMIKA_SMOKE
        cardNumberHashmap[307] = CardName.HIMIKA_REDBULLET
        cardNumberHashmap[308] = CardName.HIMIKA_CRIMSONZERO
        cardNumberHashmap[309] = CardName.HIMIKA_SCARLETIMAGINE
        cardNumberHashmap[310] = CardName.HIMIKA_BURMILIONFIELD

        cardNumberHashmap[400] = CardName.TOKOYO_BITSUNERIGI
        cardNumberHashmap[401] = CardName.TOKOYO_WOOAHHANTAGUCK
        cardNumberHashmap[402] = CardName.TOKOYO_RUNNINGRABIT
        cardNumberHashmap[403] = CardName.TOKOYO_POETDANCE
        cardNumberHashmap[404] = CardName.TOKOYO_FLIPFAN
        cardNumberHashmap[405] = CardName.TOKOYO_WINDSTAGE
        cardNumberHashmap[406] = CardName.TOKOYO_SUNSTAGE
        cardNumberHashmap[407] = CardName.TOKOYO_KUON
        cardNumberHashmap[408] = CardName.TOKOYO_THOUSANDBIRD
        cardNumberHashmap[409] = CardName.TOKOYO_ENDLESSWIND
        cardNumberHashmap[410] = CardName.TOKOYO_TOKOYOMOON

        cardNumberHashmap[500] = CardName.OBORO_WIRE
        cardNumberHashmap[501] = CardName.OBORO_SHADOWCALTROP
        cardNumberHashmap[502] = CardName.OBORO_ZANGEKIRANBU
        cardNumberHashmap[503] = CardName.OBORO_NINJAWALK
        cardNumberHashmap[504] = CardName.OBORO_INDUCE
        cardNumberHashmap[505] = CardName.OBORO_CLONE
        cardNumberHashmap[506] = CardName.OBORO_BIOACTIVITY
        cardNumberHashmap[507] = CardName.OBORO_KUMASUKE
        cardNumberHashmap[508] = CardName.OBORO_TOBIKAGE
        cardNumberHashmap[509] = CardName.OBORO_ULOO
        cardNumberHashmap[510] = CardName.OBORO_MIKAZRA

        cardNumberHashmap[100000] = CardName.YUKIHI_YUKIHI
        cardNumberHashmap[600] = CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE
        cardNumberHashmap[601] = CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS
        cardNumberHashmap[602] = CardName.YUKIHI_PUSH_OUT_SLASH_PULL
        cardNumberHashmap[603] = CardName.YUKIHI_SWING_SLASH_STAB
        cardNumberHashmap[604] = CardName.YUKIHI_TURN_UMBRELLA
        cardNumberHashmap[605] = CardName.YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN
        cardNumberHashmap[606] = CardName.YUKIHI_MAKE_CONNECTION
        cardNumberHashmap[607] = CardName.YUKIHI_FLUTTERING_SNOWFLAKE
        cardNumberHashmap[608] = CardName.YUKIHI_SWAYING_LAMPLIGHT
        cardNumberHashmap[609] = CardName.YUKIHI_CLINGY_MIND
        cardNumberHashmap[610] = CardName.YUKIHI_SWIRLING_GESTURE

        cardNumberHashmap[10100] = CardName.YURINA_CHAM
        cardNumberHashmap[10101] = CardName.YURINA_ILSUM
        cardNumberHashmap[10102] = CardName.YURINA_JARUCHIGI
        cardNumberHashmap[10103] = CardName.YURINA_GUHAB
        cardNumberHashmap[10104] = CardName.YURINA_GIBACK
        cardNumberHashmap[10105] = CardName.YURINA_APDO
        cardNumberHashmap[10106] = CardName.YURINA_GIYENBANJO
        cardNumberHashmap[10107] = CardName.YURINA_WOLYUNGNACK
        cardNumberHashmap[10108] = CardName.YURINA_POBARAM
        cardNumberHashmap[10109] = CardName.YURINA_JJOCKBAE
        cardNumberHashmap[10110] = CardName.YURINA_JURUCK

        cardNumberHashmap[10200] = CardName.SAINE_DOUBLEBEGI
        cardNumberHashmap[10201] = CardName.SAINE_HURUBEGI
        cardNumberHashmap[10202] = CardName.SAINE_MOOGECHOO
        cardNumberHashmap[10203] = CardName.SAINE_GANPA
        cardNumberHashmap[10204] = CardName.SAINE_GWONYUCK
        cardNumberHashmap[10205] = CardName.SAINE_CHOONGEMJUNG
        cardNumberHashmap[10206] = CardName.SAINE_MOOEMBUCK
        cardNumberHashmap[10207] = CardName.SAINE_YULDONGHOGEK
        cardNumberHashmap[10208] = CardName.SAINE_HANGMUNGGONGJIN
        cardNumberHashmap[10209] = CardName.SAINE_EMMOOSHOEBING
        cardNumberHashmap[10210] = CardName.SAINE_JONGGEK

        cardNumberHashmap[10300] = CardName.HIMIKA_SHOOT
        cardNumberHashmap[10301] = CardName.HIMIKA_RAPIDFIRE
        cardNumberHashmap[10302] = CardName.HIMIKA_MAGNUMCANON
        cardNumberHashmap[10303] = CardName.HIMIKA_FULLBURST
        cardNumberHashmap[10304] = CardName.HIMIKA_BACKSTEP
        cardNumberHashmap[10305] = CardName.HIMIKA_BACKDRAFT
        cardNumberHashmap[10306] = CardName.HIMIKA_SMOKE
        cardNumberHashmap[10307] = CardName.HIMIKA_REDBULLET
        cardNumberHashmap[10308] = CardName.HIMIKA_CRIMSONZERO
        cardNumberHashmap[10309] = CardName.HIMIKA_SCARLETIMAGINE
        cardNumberHashmap[10310] = CardName.HIMIKA_BURMILIONFIELD

        cardNumberHashmap[10401] = CardName.TOKOYO_WOOAHHANTAGUCK
        cardNumberHashmap[10402] = CardName.TOKOYO_RUNNINGRABIT
        cardNumberHashmap[10403] = CardName.TOKOYO_POETDANCE
        cardNumberHashmap[10404] = CardName.TOKOYO_FLIPFAN
        cardNumberHashmap[10405] = CardName.TOKOYO_WINDSTAGE
        cardNumberHashmap[10406] = CardName.TOKOYO_SUNSTAGE
        cardNumberHashmap[10407] = CardName.TOKOYO_KUON
        cardNumberHashmap[10408] = CardName.TOKOYO_THOUSANDBIRD
        cardNumberHashmap[10409] = CardName.TOKOYO_ENDLESSWIND
        cardNumberHashmap[10410] = CardName.TOKOYO_TOKOYOMOON

        cardNumberHashmap[10500] = CardName.OBORO_WIRE
        cardNumberHashmap[10501] = CardName.OBORO_SHADOWCALTROP
        cardNumberHashmap[10502] = CardName.OBORO_ZANGEKIRANBU
        cardNumberHashmap[10503] = CardName.OBORO_NINJAWALK
        cardNumberHashmap[10504] = CardName.OBORO_INDUCE
        cardNumberHashmap[10505] = CardName.OBORO_CLONE
        cardNumberHashmap[10506] = CardName.OBORO_BIOACTIVITY
        cardNumberHashmap[10507] = CardName.OBORO_KUMASUKE
        cardNumberHashmap[10508] = CardName.OBORO_TOBIKAGE
        cardNumberHashmap[10509] = CardName.OBORO_ULOO
        cardNumberHashmap[10510] = CardName.OBORO_MIKAZRA

        cardNumberHashmap[200000] = CardName.YUKIHI_YUKIHI
        cardNumberHashmap[10600] = CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE
        cardNumberHashmap[10601] = CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS
        cardNumberHashmap[10602] = CardName.YUKIHI_PUSH_OUT_SLASH_PULL
        cardNumberHashmap[10603] = CardName.YUKIHI_SWING_SLASH_STAB
        cardNumberHashmap[10604] = CardName.YUKIHI_TURN_UMBRELLA
        cardNumberHashmap[10605] = CardName.YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN
        cardNumberHashmap[10606] = CardName.YUKIHI_MAKE_CONNECTION
        cardNumberHashmap[10607] = CardName.YUKIHI_FLUTTERING_SNOWFLAKE
        cardNumberHashmap[10608] = CardName.YUKIHI_SWAYING_LAMPLIGHT
        cardNumberHashmap[10609] = CardName.YUKIHI_CLINGY_MIND
        cardNumberHashmap[10610] = CardName.YUKIHI_SWIRLING_GESTURE
    }

    suspend fun selectDustToDistance(nowCommand: CommandEnum, game_status: GameStatus): Boolean{
        if(nowCommand == CommandEnum.SELECT_ONE){
            game_status.dustToDistance(1)
            return true
        }
        else if(nowCommand == CommandEnum.SELECT_TWO){
            game_status.distanceToDust(1)
            return true
        }
        return false
    }

    private val unused = CardData(CardClass.NORMAL, CardName.CARD_UNNAME, MegamiEnum.YURINA, CardType.UNDEFINED, SubType.NONE)

    private val cham = CardData(CardClass.NORMAL, CardName.YURINA_CHAM, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    private val ilsom = CardData(CardClass.NORMAL, CardName.YURINA_ILSUM, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    private val jaru_chigi = CardData(CardClass.NORMAL, CardName.YURINA_JARUCHIGI, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    private val guhab = CardData(CardClass.NORMAL, CardName.YURINA_GUHAB, MegamiEnum.YURINA, CardType.ATTACK, SubType.FULLPOWER)
    private val giback = CardData(CardClass.NORMAL, CardName.YURINA_GIBACK, MegamiEnum.YURINA, CardType.BEHAVIOR, SubType.NONE)
    private val apdo = CardData(CardClass.NORMAL, CardName.YURINA_APDO, MegamiEnum.YURINA, CardType.ENCHANTMENT, SubType.NONE)
    private val giyenbanzo = CardData(CardClass.NORMAL, CardName.YURINA_GIYENBANJO, MegamiEnum.YURINA, CardType.ENCHANTMENT, SubType.FULLPOWER)
    private val wolyungnack = CardData(CardClass.SPECIAL, CardName.YURINA_WOLYUNGNACK, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    private val jjockbae = CardData(CardClass.SPECIAL, CardName.YURINA_JJOCKBAE, MegamiEnum.YURINA, CardType.BEHAVIOR, SubType.NONE)
    private val pobaram = CardData(CardClass.SPECIAL, CardName.YURINA_POBARAM, MegamiEnum.YURINA, CardType.ATTACK, SubType.REACTION)
    private val juruck = CardData(CardClass.SPECIAL, CardName.YURINA_JURUCK, MegamiEnum.YURINA, CardType.ATTACK, SubType.FULLPOWER)

    private fun gulSa(player: PlayerEnum, game_status: GameStatus): Boolean{
        return game_status.getPlayerLife(player) <= 3
    }
    private fun yurinaCardInit(){
        cham.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 3, 1)
        ilsom.setAttack(DistanceType.CONTINUOUS, Pair(3, 3), null, 2, 2)
        ilsom.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            if (gulSa(player, game_status)) {
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true}, {madeAttack ->
                    madeAttack.auraPlusMinus(1)
                }))
            }
            null
        })
        jaru_chigi.setAttack(DistanceType.CONTINUOUS, Pair(1, 2), null, 2, 1)
        jaru_chigi.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _ ->
            if (gulSa(player, game_status)) {
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS, {_, _, _ -> true}, {madeAttack ->
                    madeAttack.auraPlusMinus(1)
                }))
            }
            null
        })
        guhab.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 4, 3)
        guhab.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true}, {madeAttack ->
                if(game_status.getDistance() <= 2){
                    madeAttack.lifePlusMinus(-1); madeAttack.auraPlusMinus(-1)
                }
            }))
            null
        })
        giback.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) {_, player, game_status, _ ->
            game_status.addConcentration(player)
            null
        })
        giback.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.ADD, {_, _, attack -> (attack.megami != MegamiEnum.YURINA) && (attack.card_class != CardClass.SPECIAL)},
                { attack -> attack.plusMinusRange(1, true)

            }))
            game_status.addThisTurnAttackBuff(player, Buff(card_number,1, BufTag.CHANGE_EACH, { _, _, attack -> (attack.megami != MegamiEnum.YURINA) && (attack.card_class != CardClass.SPECIAL)},
                { attack -> attack.canNotReactNormal()
                }))
            null
        })
        apdo.setEnchantment(2)
        apdo.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHASM, null))
        apdo.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            game_status.addPreAttackZone(player, MadeAttack(card_number, CardClass.NORMAL, DistanceType.CONTINUOUS, 3,  999, Pair(1, 4), null, MegamiEnum.YURINA))
            game_status.afterMakeAttack(card_number, player, null)
            null
        })
        giyenbanzo.setEnchantment(4)
        giyenbanzo.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, { _, _, _ -> true}, { madeAttack ->
                if(madeAttack.megami != MegamiEnum.YURINA && gulSa(player, game_status)) madeAttack.run {
                    Chogek(); auraPlusMinus(1); lifePlusMinus(1)
                }
            }))
            null
        })
        wolyungnack.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 3, 4)
        wolyungnack.setSpecial(7)
        pobaram.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 2, 999)
        pobaram.setSpecial(3)
        pobaram.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_REDUCE){_, _, _, reactedAttack ->
            reactedAttack?.auraPlusMinus(-2)
            null
        })
        pobaram.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.TERMINATION){_, _, _, _->
            null
        })
        jjockbae.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            game_status.dustToAura(player, 5)
            null
        })
        jjockbae.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){card_number, player, game_status, _ ->
            game_status.addImmediateLifeListener(player, ImmediateBackListener(
                card_number
            ) { before, after, _ ->
                before > 3 && after <= 3
            })
            null
        })
        jjockbae.setSpecial(2)
        juruck.setAttack(DistanceType.CONTINUOUS, Pair(1, 4), null, 5, 5)
        juruck.setSpecial(5)
        juruck.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){_, player, game_status, _ ->
            if(gulSa(player, game_status)) 1
            else 0
        })
    }

    private val doublebegi = CardData(CardClass.NORMAL, CardName.SAINE_DOUBLEBEGI, MegamiEnum.SAINE, CardType.ATTACK, SubType.NONE)
    private val hurubegi = CardData(CardClass.NORMAL, CardName.SAINE_HURUBEGI, MegamiEnum.SAINE, CardType.ATTACK, SubType.REACTION)
    private val moogechoo = CardData(CardClass.NORMAL, CardName.SAINE_MOOGECHOO, MegamiEnum.SAINE, CardType.ATTACK, SubType.REACTION)
    private val ganpa = CardData(CardClass.NORMAL, CardName.SAINE_GANPA, MegamiEnum.SAINE, CardType.BEHAVIOR, SubType.NONE)
    private val gwonyuck = CardData(CardClass.NORMAL, CardName.SAINE_GWONYUCK, MegamiEnum.SAINE, CardType.ENCHANTMENT, SubType.NONE)
    private val choongemjung = CardData(CardClass.NORMAL, CardName.SAINE_CHOONGEMJUNG, MegamiEnum.SAINE, CardType.ENCHANTMENT, SubType.REACTION)
    private val mooembuck = CardData(CardClass.NORMAL, CardName.SAINE_MOOEMBUCK, MegamiEnum.SAINE, CardType.ENCHANTMENT, SubType.FULLPOWER)
    private val yuldonghogek = CardData(CardClass.SPECIAL, CardName.SAINE_YULDONGHOGEK, MegamiEnum.SAINE, CardType.BEHAVIOR, SubType.NONE)
    private val hangmunggongjin = CardData(CardClass.SPECIAL, CardName.SAINE_HANGMUNGGONGJIN, MegamiEnum.SAINE, CardType.BEHAVIOR, SubType.NONE)
    private val emmooshoebing = CardData(CardClass.SPECIAL, CardName.SAINE_EMMOOSHOEBING, MegamiEnum.SAINE, CardType.ATTACK, SubType.REACTION)
    private val jonggek = CardData(CardClass.SPECIAL, CardName.SAINE_JONGGEK, MegamiEnum.SAINE, CardType.ATTACK, SubType.REACTION)

    private fun palSang(player: PlayerEnum, game_status: GameStatus): Boolean{
        return game_status.getPlayerAura(player) <= 1
    }
    private fun saineCardInit(){
        doublebegi.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 2, 1)
        doublebegi.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            if(palSang(player, game_status)){
                game_status.addPreAttackZone(player, MadeAttack(card_number, CardClass.NORMAL, DistanceType.CONTINUOUS, 2,  1, Pair(4, 5), null, MegamiEnum.SAINE))
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        hurubegi.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 3, 1)
        moogechoo.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 2, 1)
        moogechoo.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            if(palSang(player, game_status)){
                game_status.dustToDistance(1)
            }
            null
        })
        ganpa.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.CAN_REACTABLE) {_, player, game_status, _ ->
            if(palSang(player, game_status)) 1
            else 0
        })
        ganpa.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, card_number)
                if(selectDustToDistance(nowCommand, game_status)) break
            }
            null
        })
        gwonyuck.setEnchantment(2)
        gwonyuck.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.THIS_CARD_NAP_LOCATION_CHANGE) {_, _, _, _ ->
            LocationEnum.DISTANCE.real_number
        })
        gwonyuck.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHANGE_SWELL_DISTANCE) {_, _, _, _ ->
            1
        })
        choongemjung.setEnchantment(1)
        choongemjung.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.REACT_ATTACK_REDUCE) {_, _, _, reactedAttack ->
            reactedAttack?.auraPlusMinus(-1)
            null
        })
        choongemjung.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            game_status.addPreAttackZone(player, MadeAttack(card_number, CardClass.NORMAL, DistanceType.CONTINUOUS, 1,  999, Pair(0, 10), null, MegamiEnum.SAINE,
                cannot_react_normal = false,
                cannot_react_special = false,
                cannot_react = true
            ))
            game_status.afterMakeAttack(card_number, player, null)
            null
        })
        choongemjung.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_SAKURA_TOKEN) {_, _, game_status, _ ->
            game_status.dustToDistance(1)
            null
        })
        mooembuck.setEnchantment(5)
        //-1 means every nap token can use as aura
        mooembuck.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.DAMAGE_AURA_REPLACEABLE_HERE) {_, _, _, _ ->
            null
        })
        yuldonghogek.setSpecial(6)
        yuldonghogek.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){card_number, player, game_status, _ ->
            game_status.addPreAttackZone(player, MadeAttack(card_number, CardClass.NORMAL, DistanceType.CONTINUOUS, 1,  1, Pair(3, 4), null, MegamiEnum.SAINE))
            game_status.afterMakeAttack(card_number, player, null)
            null
        })
        yuldonghogek.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){card_number,  player, game_status, _ ->
            game_status.addPreAttackZone(player, MadeAttack(card_number, CardClass.NORMAL, DistanceType.CONTINUOUS, 1,  1, Pair(4, 5), null, MegamiEnum.SAINE))
            game_status.afterMakeAttack(card_number, player, null)
            null
        })
        yuldonghogek.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){card_number, player, game_status, _ ->
            game_status.addPreAttackZone(player, MadeAttack(card_number, CardClass.NORMAL, DistanceType.CONTINUOUS, 2,  2, Pair(3, 5), null, MegamiEnum.SAINE))
            game_status.afterMakeAttack(card_number, player, null)
            null
        })
        hangmunggongjin.setSpecial(8)
        hangmunggongjin.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_BUFF) {card_number, player, game_status, _->
            game_status.addThisTurnCostBuff(player, CostBuff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, card -> (card.card_data.card_name == CardName.SAINE_HANGMUNGGONGJIN)}, {cost ->
                cost - game_status.getPlayerAura(player.Opposite())
            }))
            null
        })
        hangmunggongjin.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            game_status.auraToDistance(player.Opposite(), 2)
            null
        })
        emmooshoebing.setSpecial(2)
        emmooshoebing.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 1, 1)
        emmooshoebing.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_REDUCE){_, _, _, reactedAttack ->
            reactedAttack?.auraPlusMinus(-1)
            reactedAttack?.lifePlusMinus(-1)
            null
        })
        //return 1 means it can be return 0 means it can't be return
        emmooshoebing.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){_, player, game_status, _ ->
            if(game_status.getPlayerAura(player) <= 1) 1
            else 0
        })
        jonggek.setSpecial(5)
        jonggek.setAttack(DistanceType.CONTINUOUS, Pair(1, 5), null, 5, 5)
        jonggek.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION) {_, _, _, reactedAttack->
            if(reactedAttack != null && reactedAttack.card_class == CardClass.SPECIAL) 1
            else 0
        })
    }

    private fun yeonwhaAttack(player: PlayerEnum, game_status: GameStatus): Boolean{
        return game_status.logger.playerUseCardNumber(player) >= 2
    }

    private fun yeonwha(player: PlayerEnum, game_status: GameStatus): Boolean{
        return game_status.logger.playerUseCardNumber(player) >= 3
    }

    private val shoot = CardData(CardClass.NORMAL, CardName.HIMIKA_SHOOT, MegamiEnum.HIMIKA, CardType.ATTACK, SubType.NONE)
    private val rapidfire = CardData(CardClass.NORMAL, CardName.HIMIKA_RAPIDFIRE, MegamiEnum.HIMIKA, CardType.ATTACK, SubType.NONE)
    private val magnumcanon = CardData(CardClass.NORMAL, CardName.HIMIKA_MAGNUMCANON, MegamiEnum.HIMIKA, CardType.ATTACK, SubType.NONE)
    private val fullburst = CardData(CardClass.NORMAL, CardName.HIMIKA_FULLBURST, MegamiEnum.HIMIKA, CardType.ATTACK, SubType.FULLPOWER)
    private val backstep = CardData(CardClass.NORMAL, CardName.HIMIKA_BACKSTEP, MegamiEnum.HIMIKA, CardType.BEHAVIOR, SubType.NONE)
    private val backdraft = CardData(CardClass.NORMAL, CardName.HIMIKA_BACKDRAFT, MegamiEnum.HIMIKA, CardType.BEHAVIOR, SubType.NONE)
    private val smoke = CardData(CardClass.NORMAL, CardName.HIMIKA_SMOKE, MegamiEnum.HIMIKA, CardType.ENCHANTMENT, SubType.NONE)
    private val redbullet = CardData(CardClass.SPECIAL, CardName.HIMIKA_REDBULLET, MegamiEnum.HIMIKA, CardType.ATTACK, SubType.NONE)
    private val crimsonzero = CardData(CardClass.SPECIAL, CardName.HIMIKA_CRIMSONZERO, MegamiEnum.HIMIKA, CardType.ATTACK, SubType.NONE)
    private val scarletimagine = CardData(CardClass.SPECIAL, CardName.HIMIKA_SCARLETIMAGINE, MegamiEnum.HIMIKA, CardType.BEHAVIOR, SubType.NONE)
    private val burmilionfield = CardData(CardClass.SPECIAL, CardName.HIMIKA_BURMILIONFIELD, MegamiEnum.HIMIKA, CardType.BEHAVIOR, SubType.NONE)

    private fun himikaCardInit(){
        shoot.setAttack(DistanceType.CONTINUOUS, Pair(4, 10), null, 2, 1)
        rapidfire.setAttack(DistanceType.CONTINUOUS, Pair(6, 8), null, 2, 1)
        rapidfire.addtext((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            if (yeonwhaAttack(player, game_status)) {
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true}, {madeAttack ->
                    madeAttack.auraPlusMinus(1)
                    madeAttack.lifePlusMinus(1)
                }))
            }
            null
        }))
        magnumcanon.setAttack(DistanceType.CONTINUOUS, Pair(5, 8), null, 3, 2)
        magnumcanon.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            if(game_status.lifeToDust(player, 1)){
                game_status.gameEnd(player.Opposite())
            }
            null
        })
        fullburst.setAttack(DistanceType.CONTINUOUS, Pair(5, 9), null, 3, 1)
        fullburst.addtext((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CHANGE_EACH_IMMEDIATE, {_, _, _ -> true}, {madeAttack ->
                madeAttack.setBothSideDamage()
            }))
            null
        }))
        backstep.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.DRAW_CARD) {_, player, game_status, _ ->
            game_status.drawCard(player, 1)
            null
        })
        backstep.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, _, game_status, _ ->
            game_status.dustToDistance(1)
            null
        })
        backdraft.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_SHRINK) {_, player, game_status, _ ->
            game_status.setShrink(player.Opposite())
            null
        })
        backdraft.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            if(yeonwha(player, game_status)){
                game_status.addThisTurnAttackBuff(player, Buff(card_number,1, BufTag.PLUS_MINUS, {_, _, attack -> (attack.megami != MegamiEnum.HIMIKA) && (attack.aura_damage != 999)},
                    { attack -> attack.run{
                        auraPlusMinus(1); lifePlusMinus(1)
                    }
                    }))
            }
            null
        })
        smoke.setEnchantment(3)
        //FORBID_MOVE_TOKEN return FromLocationEnum * 100 + ToLocationEnum (if anywhere it will be 99)
        smoke.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.FORBID_MOVE_TOKEN){_, _, _, _ ->
            LocationEnum.DISTANCE.real_number * 100 + 99
            null
        })
        redbullet.setAttack(DistanceType.CONTINUOUS, Pair(5, 10), null, 3, 1)
        redbullet.setSpecial(0)
        crimsonzero.setAttack(DistanceType.CONTINUOUS, Pair(0, 2), null, 2, 2)
        crimsonzero.setSpecial(5)
        crimsonzero.addtext((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CHANGE_EACH_IMMEDIATE, {_, _, _ -> true}, {madeAttack ->
                madeAttack.setBothSideDamage()
            }))
            null
        }))
        crimsonzero.addtext((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            if(game_status.getDistance() == 0){
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CHANGE_EACH_IMMEDIATE, {_, _, _ -> true}, {madeAttack ->
                    madeAttack.canNotReact()
                }))
            }
            null
        }))
        scarletimagine.setSpecial(3)
        scarletimagine.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.DRAW_CARD) {_, player, game_status, _->
            game_status.drawCard(player, 3)
            null
        })
        scarletimagine.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CARD_TO_COVER) {_, player, game_status, _->
            game_status.coverCard(player, player)
            null
        })
        burmilionfield.setSpecial(2)
        burmilionfield.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.DRAW_CARD) {_, player, game_status, _->
            if(yeonwha(player, game_status)){
                game_status.dustToDistance(2)
            }
            null
        })
        burmilionfield.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){_, player, game_status, _ ->
            if(game_status.getPlayerHandSize(player) == 0) 1
            else 0
        })
    }

    private fun kyochi(player: PlayerEnum, game_status: GameStatus): Boolean{
        return game_status.getConcentration(player) == 2
    }

    private val bitsunerigi = CardData(CardClass.NORMAL, CardName.TOKOYO_BITSUNERIGI, MegamiEnum.TOKOYO, CardType.ATTACK, SubType.NONE)
    private val wooahhantaguck = CardData(CardClass.NORMAL, CardName.TOKOYO_WOOAHHANTAGUCK, MegamiEnum.TOKOYO, CardType.ATTACK, SubType.REACTION)
    private val runningrabit = CardData(CardClass.NORMAL, CardName.TOKOYO_RUNNINGRABIT, MegamiEnum.TOKOYO, CardType.BEHAVIOR, SubType.NONE)
    private val poetdance = CardData(CardClass.NORMAL, CardName.TOKOYO_POETDANCE, MegamiEnum.TOKOYO, CardType.BEHAVIOR, SubType.REACTION)
    private val flipfan = CardData(CardClass.NORMAL, CardName.TOKOYO_FLIPFAN, MegamiEnum.TOKOYO, CardType.BEHAVIOR, SubType.FULLPOWER)
    private val windstage = CardData(CardClass.NORMAL, CardName.TOKOYO_WINDSTAGE, MegamiEnum.TOKOYO, CardType.ENCHANTMENT, SubType.NONE)
    private val sunstage = CardData(CardClass.NORMAL, CardName.TOKOYO_SUNSTAGE, MegamiEnum.TOKOYO, CardType.ENCHANTMENT, SubType.NONE)
    private val kuon = CardData(CardClass.SPECIAL, CardName.TOKOYO_KUON, MegamiEnum.TOKOYO, CardType.ATTACK, SubType.REACTION)
    private val thousandbird = CardData(CardClass.SPECIAL, CardName.TOKOYO_THOUSANDBIRD, MegamiEnum.TOKOYO, CardType.ATTACK, SubType.NONE)
    private val endlesswind = CardData(CardClass.SPECIAL, CardName.TOKOYO_ENDLESSWIND, MegamiEnum.TOKOYO, CardType.ATTACK, SubType.NONE)
    private val tokoyomoon = CardData(CardClass.SPECIAL, CardName.TOKOYO_TOKOYOMOON, MegamiEnum.TOKOYO, CardType.BEHAVIOR, SubType.NONE)

    private fun tokoyoCardInit(){
        bitsunerigi.setAttack(DistanceType.CONTINUOUS, Pair(4, 4), null, 999, 1)
        bitsunerigi.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CARD_DISCARD_PLACE_CHANGE) {card_number, player, game_status, _ ->
            if(kyochi(player, game_status)){
                game_status.movePlayingCard(player, LocationEnum.YOUR_DECK_TOP, card_number)
            }
            null
        })
        wooahhantaguck.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 2, 1)
        wooahhantaguck.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_INVALID) {_, player, game_status, reactedAttack ->
            if(kyochi(player, game_status) && reactedAttack?.card_class != CardClass.SPECIAL){
                reactedAttack?.makeNotValid()
            }
            null
        })
        runningrabit.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, _, game_status, _->
            if(game_status.getDistance() <= 3){
                game_status.dustToDistance(2)
            }
            null
        })
        poetdance.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            game_status.addConcentration(player)
            null
        })
        poetdance.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, card_number)
                if(nowCommand == CommandEnum.SELECT_ONE){
                    game_status.flareToSelfAura(player, 1)
                    break
                }
                else if(nowCommand == CommandEnum.SELECT_TWO){
                    game_status.auraToDistance(player, 1)
                    break
                }
            }
            null
        })
        flipfan.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {_, player, game_status, _->
            while (true){
                val set = mutableSetOf<Int>()
                val list = game_status.selectCardFrom(player, player, listOf(LocationEnum.COVER_CARD, LocationEnum.DISCARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT)
                set.addAll(list)
                if (set.size <= 2){
                    for (cardNumber in list){
                        game_status.popCardFrom(player, cardNumber, LocationEnum.DISCARD, true)?.let {
                            game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, true)
                        }?: game_status.popCardFrom(player, cardNumber, LocationEnum.COVER_CARD, false)?.let {
                            game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, false)
                        }
                    }
                    break
                }
            }
            null
        })
        flipfan.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _->
            game_status.dustToAura(player, 2)
            null
        })
        windstage.setEnchantment(2)
        windstage.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.distanceToAura(player, 2)
            null
        })
        windstage.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.auraToDistance(player, 2)
            null
        })
        sunstage.setEnchantment(2)
        sunstage.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.TERMINATION){_, _, _, _->
            null
        })
        sunstage.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.CHANGE_CONCENTRATION) {_, player, game_status, _ ->
            game_status.setConcentration(player, 2)
            null
        })
        sunstage.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            game_status.addPreAttackZone(player, MadeAttack(card_number, CardClass.NORMAL, DistanceType.CONTINUOUS, 999,  1, Pair(3, 6), null, MegamiEnum.TOKOYO))
            game_status.afterMakeAttack(card_number, player, null)
            null
        })
        kuon.setSpecial(5)
        kuon.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CARD_DISCARD_PLACE_CHANGE) {_, player, game_status, reactedAttack ->
            if(kyochi(player, game_status)){
                reactedAttack?.makeNotValid()
            }
            null
        })
        thousandbird.setSpecial(2)
        thousandbird.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 2, 2)
        thousandbird.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.RECONSTRUCT) {_, player, game_status, _ ->
            game_status.deckReconstruct(player, false)
            null
        })
        endlesswind.setSpecial(1)
        endlesswind.setAttack(DistanceType.CONTINUOUS, Pair(3, 8), null, 1, 1)
        endlesswind.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CHANGE_EACH_IMMEDIATE, {_, _, _ -> true}, {madeAttack ->
                madeAttack.canNotReact()
            }))
            null
        })
        endlesswind.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {_, player, game_status, _ ->
            if (game_status.endlessWindCheck(player.Opposite())){
                game_status.showSome(player.Opposite(), CommandEnum.SHOW_HAND_ALL_YOUR, -1)
            }
            else{
                while (true){
                    val list = game_status.selectCardFrom(player.Opposite(), player.Opposite(), listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT)
                    println(list)
                    if (list.size == 1){
                        if(cardNumberHashmap[list[0]]?.let {returnCardDataByName(it).canDiscard && returnCardDataByName(it).card_type != CardType.ATTACK } == true){
                            val card = game_status.popCardFrom(player.Opposite(), list[0], LocationEnum.HAND, true)?: continue
                            game_status.insertCardTo(player, card, LocationEnum.DISCARD, true)
                            break
                        }
                    }
                }
            }
            null
        })
        endlesswind.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){_, player, game_status, _ ->
            if(kyochi(player, game_status)) 1
            else 0
        })
        tokoyomoon.setSpecial(2)
        tokoyomoon.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _->
            game_status.setConcentration(player, 2)
            game_status.setConcentration(player.Opposite(), 0)
            game_status.setShrink(player.Opposite())
            null
        })
    }

    private val wire = CardData(CardClass.NORMAL, CardName.OBORO_WIRE, MegamiEnum.OBORO, CardType.ATTACK, SubType.NONE)
    private val shadowcaltrop = CardData(CardClass.NORMAL, CardName.OBORO_SHADOWCALTROP, MegamiEnum.OBORO, CardType.ATTACK, SubType.NONE)
    private val zangekiranbu = CardData(CardClass.NORMAL, CardName.OBORO_ZANGEKIRANBU, MegamiEnum.OBORO, CardType.ATTACK, SubType.FULLPOWER)
    private val ninjawalk = CardData(CardClass.NORMAL, CardName.OBORO_NINJAWALK, MegamiEnum.OBORO, CardType.BEHAVIOR, SubType.NONE)
    private val induce = CardData(CardClass.NORMAL, CardName.OBORO_INDUCE, MegamiEnum.OBORO, CardType.BEHAVIOR, SubType.REACTION)
    private val clone = CardData(CardClass.NORMAL, CardName.OBORO_CLONE, MegamiEnum.OBORO, CardType.BEHAVIOR, SubType.FULLPOWER)
    private val bioactivity = CardData(CardClass.NORMAL, CardName.OBORO_BIOACTIVITY, MegamiEnum.OBORO, CardType.ENCHANTMENT, SubType.NONE)
    private val kumasuke = CardData(CardClass.SPECIAL, CardName.OBORO_KUMASUKE, MegamiEnum.OBORO, CardType.ATTACK, SubType.FULLPOWER)
    private val tobikage = CardData(CardClass.SPECIAL, CardName.OBORO_TOBIKAGE, MegamiEnum.OBORO, CardType.BEHAVIOR, SubType.REACTION)
    private val uloo = CardData(CardClass.SPECIAL, CardName.OBORO_ULOO, MegamiEnum.OBORO, CardType.BEHAVIOR, SubType.NONE)
    private val mikazra = CardData(CardClass.SPECIAL, CardName.OBORO_MIKAZRA, MegamiEnum.OBORO, CardType.ATTACK, SubType.NONE)

    private fun oboroCardInit(){
        wire.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 2, 2)
        wire.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.INSTALLATION) {_, _, _, _->
            null
        })
        shadowcaltrop.setAttack(DistanceType.CONTINUOUS, Pair(2, 2), null, 2, 1)
        shadowcaltrop.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.INSTALLATION) {_, _, _, _->
            null
        })
        shadowcaltrop.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CHANGE_EACH_IMMEDIATE, {_, _, _ -> true}, {madeAttack ->
                madeAttack.canNotReact()
            }))
            null
        })
        shadowcaltrop.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _ ->
            if (game_status.logger.checkThisCardUseInCover(player, card_number)){
                game_status.coverCard(player.Opposite(), player)
            }
            null
        })
        zangekiranbu.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 3, 2)
        zangekiranbu.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            if (game_status.logger.checkThisTurnGetAuraDamage(player.Opposite())) {
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true}, {madeAttack ->
                    madeAttack.apply { auraPlusMinus(1); lifePlusMinus(1) }
                }))
            }
            null
        })
        ninjawalk.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.INSTALLATION, null))
        ninjawalk.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, _, game_status, _ ->
            game_status.dustToDistance(1)
            null
        })
        ninjawalk.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _ ->
            if (game_status.logger.checkThisCardUseInCover(player, card_number)){
                game_status.useInstallationOnce(player)
            }
            null
        })
        induce.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.INSTALLATION, null))
        induce.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, card_number)
                if(nowCommand == CommandEnum.SELECT_ONE){
                    game_status.distanceToAura(player.Opposite(), 1)
                    break
                }
                else if(nowCommand == CommandEnum.SELECT_TWO){
                    game_status.auraToFlare(player.Opposite(), player.Opposite(), 1)
                    break
                }
            }
            null
        })
        clone.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) {_, player, game_status, _ ->
            if(game_status.checkCoverFullPower(player)){
                game_status.showSome(player, CommandEnum.SHOW_COVER_YOUR, -1)
            }
            else{
                while(true){
                    val selected = game_status.selectCardFrom(player, player, listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT)
                    if(selected.size == 1){
                        val selectNumber = selected[0]
                        val card = game_status.getCardFrom(player, selectNumber, LocationEnum.COVER_CARD)?: continue
                        if(card.card_data.sub_type == SubType.FULLPOWER) continue
                        game_status.useCardFrom(player, card, LocationEnum.COVER_CARD, false, null)
                        if(game_status.getEndTurn(player)) break
                        val secondCard = game_status.getCardFrom(player, selectNumber, LocationEnum.DISCARD)?: break
                        game_status.useCardFrom(player, secondCard, LocationEnum.DISCARD, false, null)
                        break
                    }
                }
            }
            null
        })
        bioactivity.setEnchantment(4)
        bioactivity.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.INSTALLATION, null))
        bioactivity.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHASM, null))
        bioactivity.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.RETURN_OTHER_CARD) {_, player, game_status, _ ->
            if(game_status.getPlayer(player).used_special_card.isEmpty()) {
                null
            }
            else{
                while(true){
                    val selected = game_status.selectCardFrom(player, player, listOf(LocationEnum.USED_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT)
                    if(selected.size == 1 && game_status.returnSpecialCard(player, selected[0])){
                        break
                    }
                }
                null
            }
        })
        kumasuke.setSpecial(4)
        kumasuke.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 2, 2)
        kumasuke.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            for (i in 1..game_status.getPlayer(player).cover_card.size){
                game_status.addPreAttackZone(player, MadeAttack(card_number, CardClass.NORMAL, DistanceType.CONTINUOUS, 2,  2, Pair(3, 4), null, MegamiEnum.OBORO))
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        tobikage.setSpecial(4)
        tobikage.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) {_, player, game_status, react_attack ->
            if(game_status.checkCoverFullPower(player)){
                game_status.showSome(player, CommandEnum.SHOW_COVER_YOUR, -1)
            }
            else{
                while(true){
                    val selected = game_status.selectCardFrom(player, player, listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT)
                    if(selected.size == 1){
                        val selectNumber = selected[0]
                        val card = game_status.getCardFrom(player, selectNumber, LocationEnum.COVER_CARD)?: continue
                        if(card.card_data.sub_type == SubType.FULLPOWER) continue
                        game_status.useCardFrom(player, card, LocationEnum.COVER_CARD, true, react_attack)
                        break
                    }
                }
            }
            null
        })
        uloo.setSpecial(4)
        uloo.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.INSTALLATION_INFINITE, null))
        mikazra.setSpecial(0)
        mikazra.setAttack(DistanceType.CONTINUOUS, Pair(3, 7), null, 1, 1)
        mikazra.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.dustToFlare(player, 1)
            null
        })
        mikazra.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){_, player, game_status, _ ->
            if(game_status.getPlayerFlare(player) == 0) 1
            else 0
        })
    }

    private val yukihi = CardData(CardClass.SPECIAL, CardName.YUKIHI_YUKIHI, MegamiEnum.YUKIHI, CardType.BEHAVIOR, SubType.NONE)

    private val hiddenNeedle = CardData(CardClass.NORMAL, CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.NONE)
    private val hiddenFire = CardData(CardClass.NORMAL, CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.NONE)
    private val pushOut = CardData(CardClass.NORMAL, CardName.YUKIHI_PUSH_OUT_SLASH_PULL, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.NONE)
    private val swing = CardData(CardClass.NORMAL, CardName.YUKIHI_SWING_SLASH_STAB, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.FULLPOWER)
    private val turnUmbrella = CardData(CardClass.NORMAL, CardName.YUKIHI_TURN_UMBRELLA, MegamiEnum.YUKIHI, CardType.BEHAVIOR, SubType.NONE)
    private val backwardStep = CardData(CardClass.NORMAL, CardName.YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN, MegamiEnum.YUKIHI, CardType.BEHAVIOR, SubType.REACTION)
    private val makeConnection = CardData(CardClass.NORMAL, CardName.YUKIHI_MAKE_CONNECTION, MegamiEnum.YUKIHI, CardType.ENCHANTMENT, SubType.NONE)
    private val flutteringSnowflake = CardData(CardClass.SPECIAL, CardName.YUKIHI_FLUTTERING_SNOWFLAKE, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.NONE)
    private val swayingLamplight = CardData(CardClass.SPECIAL, CardName.YUKIHI_SWAYING_LAMPLIGHT, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.NONE)
    private val clingyMind = CardData(CardClass.SPECIAL, CardName.YUKIHI_CLINGY_MIND, MegamiEnum.YUKIHI, CardType.ENCHANTMENT, SubType.FULLPOWER)
    private val swirlingGesture = CardData(CardClass.SPECIAL, CardName.YUKIHI_SWIRLING_GESTURE, MegamiEnum.YUKIHI, CardType.BEHAVIOR, SubType.REACTION)

    private fun yukihiCardInit(){
        yukihi.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.END_TURN_EFFECT) {card_number, player, game_status, _ ->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, card_number)
                if(nowCommand == CommandEnum.SELECT_ONE){
                    game_status.changeUmbrella(player)
                    break

                }
                else if(nowCommand == CommandEnum.SELECT_TWO){
                    //not change
                    break
                }
            }
            null
        })
        hiddenNeedle.umbrellaMark = true
        hiddenNeedle.setAttackFold(DistanceType.CONTINUOUS, Pair(4, 6), null, 3, 1)
        hiddenNeedle.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 2), null, 1, 2)
        hiddenFire.umbrellaMark = true
        hiddenFire.setAttackFold(DistanceType.CONTINUOUS, Pair(5, 6), null, 1, 1)
        hiddenFire.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 2), null, 1, 1)
        hiddenFire.addTextFold(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CARD_DISCARD_PLACE_CHANGE) {card_number, player, game_status, _ ->
            game_status.movePlayingCard(player, LocationEnum.HAND, card_number)
            game_status.changeUmbrella(player)
            null
        })
        pushOut.umbrellaMark = true
        pushOut.setAttackFold(DistanceType.CONTINUOUS, Pair(2, 5), null, 1, 1)
        pushOut.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 2), null, 1, 1)
        pushOut.addTextFold(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, card_number)
                if(selectDustToDistance(nowCommand, game_status)) break
            }
            null
        })
        pushOut.addTextUnfold(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {_, _, game_status, _->
            game_status.distanceToDust(2)
            null
        })
        swing.umbrellaMark = true
        swing.setAttackFold(DistanceType.CONTINUOUS, Pair(4, 6), null, 5, 999)
        swing.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 2), null, 999, 2)
        turnUmbrella.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.SHOW_HAND_WHEN_CHANGE_UMBRELLA) {card_number, player, game_status, _->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, card_number)
                if(nowCommand == CommandEnum.SELECT_ONE){
                    game_status.showSome(player, CommandEnum.SHOW_HAND_YOUR, card_number)
                    game_status.dustToAura(player, 1)
                    break
                }
                else if(nowCommand == CommandEnum.SELECT_TWO){
                    //not show
                    break
                }
            }
            null
        })
        backwardStep.umbrellaMark = true
        backwardStep.addTextFold(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            game_status.dustToDistance(1)
            null
        })
        backwardStep.addTextUnfold(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            game_status.distanceToDust(1)
            null
        })
        makeConnection.setEnchantment(2)
        makeConnection.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            if(game_status.getUmbrella(player) == Umbrella.UNFOLD) game_status.dustToDistance(1)
            else game_status.distanceToDust(1)
            null
        })
        makeConnection.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            if(game_status.getUmbrella(player) == Umbrella.UNFOLD) game_status.distanceToDust(1)
            else game_status.dustToDistance(1)
            null
        })
        flutteringSnowflake.umbrellaMark = true
        flutteringSnowflake.setSpecial(2)
        flutteringSnowflake.setAttackFold(DistanceType.CONTINUOUS, Pair(3, 6), null, 3, 1)
        flutteringSnowflake.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 2), null, 0, 0)
        flutteringSnowflake.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){card_number, player, game_status, _ ->
            game_status.addImmediateUmbrellaListener(player, ImmediateBackListener(
                card_number
            ) { _, _, _ ->
                true
            })
            null
        })
        swayingLamplight.umbrellaMark = true
        swayingLamplight.setSpecial(5)
        swayingLamplight.setAttackFold(DistanceType.CONTINUOUS, Pair(4, 6), null, 0, 0)
        swayingLamplight.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 0), null, 4, 5)
        clingyMind.setSpecial(3)
        clingyMind.setEnchantment(7)
        clingyMind.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){card_number, player, game_status, _ ->
            game_status.addThisTurnRangeBuff(player, RangeBuff(card_number, 1, RangeBufTag.CHANGE_IMMEDIATE, { _, _, _ -> true}, { madeAttack ->
                when(cardNumberHashmap[madeAttack.card_number]){
                    CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE -> {
                        madeAttack.run { addRange(Pair(0, 2)); addRange(Pair(4, 6))}
                    }
                    CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS -> {
                        madeAttack.run { addRange(Pair(0, 2)); addRange(Pair(5, 6))}
                    }
                    CardName.YUKIHI_PUSH_OUT_SLASH_PULL -> {
                        madeAttack.run { addRange(Pair(0, 2)); addRange(Pair(2, 5))}
                    }
                    CardName.YUKIHI_SWING_SLASH_STAB -> {
                        madeAttack.run { addRange(Pair(0, 2)); addRange(Pair(4, 6))}
                    }
                    CardName.YUKIHI_FLUTTERING_SNOWFLAKE -> {
                        madeAttack.run { addRange(Pair(0, 2)); addRange(Pair(3, 6))}
                    }
                    CardName.YUKIHI_SWAYING_LAMPLIGHT -> {
                        madeAttack.run { addRange(Pair(0, 0)); addRange(Pair(4, 6))}
                    }
                    else -> {}
                }
            }))
            null
        })
        swirlingGesture.setSpecial(1)
        swirlingGesture.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.changeUmbrella(player)
            game_status.dustToAura(player, 1)
            null
        })
    }

    private val shinra = CardData(CardClass.SPECIAL, CardName.SHINRA_SHINRA, MegamiEnum.YUKIHI, CardType.BEHAVIOR, SubType.NONE)

    private fun shinraCardInit(){
        shinra.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.END_TURN_EFFECT) {card_number, player, game_status, _ ->
            if(game_status.getPlayer(player).stratagem == null){
                while(true){
                    val nowCommand = game_status.receiveCardEffectSelect(player, card_number)
                    if(nowCommand == CommandEnum.SELECT_ONE){
                        game_status.setStratagem(player, Stratagem.SHIN_SAN)
                        break

                    }
                    else if(nowCommand == CommandEnum.SELECT_TWO){
                        game_status.setStratagem(player, Stratagem.GUE_MO)
                        break
                    }
                }
            }
            null
        })
    }

    fun init(){
        hashMapInit()

        yurinaCardInit()
        saineCardInit()
        himikaCardInit()
        tokoyoCardInit()
        oboroCardInit()
        yukihiCardInit()
        shinraCardInit()
    }

    fun returnCardDataByName(card_name: CardName): CardData {
        when (card_name){
            CardName.CARD_UNNAME -> return unused
            CardName.YURINA_CHAM -> return cham
            CardName.YURINA_ILSUM -> return ilsom
            CardName.YURINA_JARUCHIGI -> return jaru_chigi
            CardName.YURINA_GUHAB -> return guhab
            CardName.YURINA_GIBACK -> return giback
            CardName.YURINA_APDO -> return apdo
            CardName.YURINA_GIYENBANJO -> return giyenbanzo
            CardName.YURINA_WOLYUNGNACK -> return wolyungnack
            CardName.YURINA_POBARAM -> return pobaram
            CardName.YURINA_JJOCKBAE -> return jjockbae
            CardName.YURINA_JURUCK -> return juruck
            CardName.SAINE_DOUBLEBEGI -> return doublebegi
            CardName.SAINE_HURUBEGI -> return hurubegi
            CardName.SAINE_MOOGECHOO -> return moogechoo
            CardName.SAINE_GANPA -> return ganpa
            CardName.SAINE_GWONYUCK -> return gwonyuck
            CardName.SAINE_CHOONGEMJUNG -> return choongemjung
            CardName.SAINE_MOOEMBUCK -> return mooembuck
            CardName.SAINE_YULDONGHOGEK -> return yuldonghogek
            CardName.SAINE_HANGMUNGGONGJIN -> return hangmunggongjin
            CardName.SAINE_EMMOOSHOEBING -> return emmooshoebing
            CardName.SAINE_JONGGEK -> return jonggek
            CardName.HIMIKA_SHOOT -> return shoot
            CardName.HIMIKA_RAPIDFIRE -> return rapidfire
            CardName.HIMIKA_MAGNUMCANON -> return magnumcanon
            CardName.HIMIKA_FULLBURST -> return fullburst
            CardName.HIMIKA_BACKSTEP -> return backstep
            CardName.HIMIKA_BACKDRAFT -> return backdraft
            CardName.HIMIKA_SMOKE -> return smoke
            CardName.HIMIKA_REDBULLET -> return redbullet
            CardName.HIMIKA_CRIMSONZERO -> return crimsonzero
            CardName.HIMIKA_SCARLETIMAGINE -> return scarletimagine
            CardName.HIMIKA_BURMILIONFIELD -> return burmilionfield
            CardName.TOKOYO_BITSUNERIGI -> return bitsunerigi
            CardName.TOKOYO_WOOAHHANTAGUCK -> return wooahhantaguck
            CardName.TOKOYO_RUNNINGRABIT -> return runningrabit
            CardName.TOKOYO_POETDANCE -> return poetdance
            CardName.TOKOYO_FLIPFAN -> return flipfan
            CardName.TOKOYO_WINDSTAGE -> return windstage
            CardName.TOKOYO_SUNSTAGE -> return sunstage
            CardName.TOKOYO_KUON -> return kuon
            CardName.TOKOYO_THOUSANDBIRD -> return thousandbird
            CardName.TOKOYO_ENDLESSWIND -> return endlesswind
            CardName.TOKOYO_TOKOYOMOON -> return tokoyomoon
            CardName.OBORO_WIRE -> return wire
            CardName.OBORO_SHADOWCALTROP -> return shadowcaltrop
            CardName.OBORO_ZANGEKIRANBU -> return zangekiranbu
            CardName.OBORO_NINJAWALK -> return ninjawalk
            CardName.OBORO_INDUCE -> return induce
            CardName.OBORO_CLONE -> return clone
            CardName.OBORO_BIOACTIVITY -> return bioactivity
            CardName.OBORO_KUMASUKE -> return kumasuke
            CardName.OBORO_TOBIKAGE -> return tobikage
            CardName.OBORO_ULOO -> return uloo
            CardName.OBORO_MIKAZRA -> return mikazra
            CardName.YUKIHI_YUKIHI -> return yukihi
            CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE -> return hiddenNeedle
            CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS -> return hiddenFire
            CardName.YUKIHI_PUSH_OUT_SLASH_PULL -> return pushOut
            CardName.YUKIHI_SWING_SLASH_STAB -> return swing
            CardName.YUKIHI_TURN_UMBRELLA -> return turnUmbrella
            CardName.YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN -> return backwardStep
            CardName.YUKIHI_MAKE_CONNECTION -> return makeConnection
            CardName.YUKIHI_FLUTTERING_SNOWFLAKE -> return flutteringSnowflake
            CardName.YUKIHI_SWAYING_LAMPLIGHT -> return swayingLamplight
            CardName.YUKIHI_CLINGY_MIND -> return clingyMind
            CardName.YUKIHI_SWIRLING_GESTURE -> return swirlingGesture
            CardName.SHINRA_SHINRA -> TODO()
            CardName.SHINRA_IBLON -> TODO()
            CardName.SHINRA_BANLON -> TODO()
            CardName.SHINRA_KIBEN -> TODO()
            CardName.SHINRA_INYONG -> TODO()
            CardName.SHINRA_SEONGDONG -> TODO()
            CardName.SHINRA_JANGDAM -> TODO()
            CardName.SHINRA_NONPA -> TODO()
            CardName.SHINRA_WANJEON_NONPA -> TODO()
            CardName.SHINRA_DASIG_IHAE -> TODO()
            CardName.SHINRA_CHEONJI_BANBAG -> TODO()
            CardName.SHINRA_SHINRA_BAN_SHO -> TODO()
        }
    }
}