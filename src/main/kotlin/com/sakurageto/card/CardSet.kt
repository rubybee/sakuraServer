package com.sakurageto.card

import com.sakurageto.gamelogic.ImmediateBackListner
import com.sakurageto.gamelogic.MegamiEnum

object CardSet {
    val cardname_hashmap_for_start_turn = HashMap<CardName, Int>()
    val cardname_hashmap_for_second_turn = HashMap<CardName, Int>()
    val cardnumber_hashmap = HashMap<CardName, Int>()

    val unused = CardData(CardClass.NORMAL, CardName.CARD_UNNAME, MegamiEnum.YURINA, CardType.UNDEFINED, SubType.NONE)

    val cham = CardData(CardClass.NORMAL, CardName.YURINA_CHAM, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    val ilsom = CardData(CardClass.NORMAL, CardName.YURINA_ILSUM, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    val jaru_chigi = CardData(CardClass.NORMAL, CardName.YURINA_JARUCHIGI, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    val guhab = CardData(CardClass.NORMAL, CardName.YURINA_GUHAB, MegamiEnum.YURINA, CardType.ATTACK, SubType.FULLPOWER)
    val giback = CardData(CardClass.NORMAL, CardName.YURINA_GIBACK, MegamiEnum.YURINA, CardType.BEHAVIOR, SubType.NONE)
    val apdo = CardData(CardClass.NORMAL, CardName.YURINA_APDO, MegamiEnum.YURINA, CardType.ENCHANTMENT, SubType.NONE)
    val giyenbanzo = CardData(CardClass.NORMAL, CardName.YURINA_GIYENBANJO, MegamiEnum.YURINA, CardType.ENCHANTMENT, SubType.FULLPOWER)
    val wolyungnack = CardData(CardClass.SPECIAL, CardName.YURINA_WOLYUNGNACK, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    val jjockbae = CardData(CardClass.SPECIAL, CardName.YURINA_JJOCKBAE, MegamiEnum.YURINA, CardType.BEHAVIOR, SubType.NONE)
    val pobaram = CardData(CardClass.SPECIAL, CardName.YURINA_POBARAM, MegamiEnum.YURINA, CardType.ATTACK, SubType.REACTION)
    val juruck = CardData(CardClass.SPECIAL, CardName.YURINA_JURUCK, MegamiEnum.YURINA, CardType.ATTACK, SubType.FULLPOWER)

    fun YurinaCardInit(){
        cham.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 3, 1)
        ilsom.setAttack(DistanceType.CONTINUOUS, Pair(3, 3), null, 2, 2)
        ilsom.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { player, game_status, _->
            if (game_status.getPlayerLife(player) <= 3) {
                game_status.addThisTurnAttackBuff(player, Buff(CardName.YURINA_ILSUM, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _ -> true}, {madeAttack ->
                    madeAttack.auraPlusMinus(1)
                }))
            }
            null
        })
        jaru_chigi.setAttack(DistanceType.CONTINUOUS, Pair(1, 2), null, 2, 1)
        jaru_chigi.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { player, game_status, _ ->
            if (game_status.getPlayerLife(player) <= 3) {
                game_status.addThisTurnAttackBuff(player, Buff(CardName.YURINA_JARUCHIGI, 1, BufTag.PLUS_MINUS, {_, _ -> true}, {madeAttack ->
                    madeAttack.auraPlusMinus(1)
                }))
            }
            null
        })
        guhab.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 4, 3)
        guhab.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(CardName.YURINA_GUHAB, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _ -> true}, {madeAttack ->
                if(game_status.distance <= 2){
                    madeAttack.lifePlusMinus(-1); madeAttack.auraPlusMinus(-1)
                }
            }))
            null
        })
        giback.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) { player, game_status, _ ->
            game_status.addConcentration(player)
            null
        })
        giback.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { player, game_status, _->
            game_status.addThisTurnRangeBuff(player, RangeBuff(CardName.YURINA_GIBACK,1, RangeBufTag.ADD, {_, _ -> true}, {madeattack ->
                madeattack.run{
                    addRange(1, true); canNotReactNormal()
                }
            }))
            null
        })
        apdo.setEnchantment(2)
        apdo.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHASM, null))
        apdo.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) {player, game_status, _ ->
            game_status.addPreAttackZone(player, MadeAttack(DistanceType.CONTINUOUS, 999,  3, Pair(1, 4), null, MegamiEnum.YURINA))
            null
        })
        giyenbanzo.setEnchantment(4)
        giyenbanzo.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){ player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(CardName.YURINA_GIYENBANJO, 1, BufTag.PLUS_MINUS_IMMEDIATE, { _, _ -> true}, { madeAttack ->
                if(madeAttack.megami != MegamiEnum.YURINA) madeAttack.run {
                    Chogek(); auraPlusMinus(1); lifePlusMinus(1)
                }
            }))
            null
        })
        wolyungnack.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 3, 4)
        wolyungnack.setSpecial(7)
        pobaram.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 2, 999)
        pobaram.setSpecial(3)
        pobaram.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_REDUCE){ player, game_status, react_attack ->
            react_attack!!.auraPlusMinus(-2)
            null
        })
        pobaram.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.END_TURN){ player, game_status, _->
            game_status.setEndTurn(player, true)
            null
        })
        jjockbae.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){ player, game_status, _ ->
            game_status.dustToAura(player, 5)
            null
        })
        jjockbae.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){player, game_status, _ ->
            game_status.addImmediateLifeListner(player, ImmediateBackListner(
                game_status.getCardNumber(player, CardName.YURINA_JJOCKBAE)
            ) { before, after, _ ->
                before > 3 && after <= 3
            })
            null
        })
        jjockbae.setSpecial(2)
        juruck.setAttack(DistanceType.CONTINUOUS, Pair(1, 4), null, 5, 5)
        juruck.setSpecial(5)
        juruck.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){player, game_status, _ ->
            if(game_status.getPlayerLife(player) <= 3){
                1
            }
            0
        })
    }

    fun hashMapInit(){
        //for first turn player 0~9999
        cardname_hashmap_for_start_turn[CardName.YURINA_CHAM] = 0
        cardname_hashmap_for_start_turn[CardName.YURINA_ILSUM] = 1
        cardname_hashmap_for_start_turn[CardName.YURINA_JARUCHIGI] = 2
        cardname_hashmap_for_start_turn[CardName.YURINA_GUHAB] = 3
        cardname_hashmap_for_start_turn[CardName.YURINA_GIBACK] = 4
        cardname_hashmap_for_start_turn[CardName.YURINA_APDO] = 5
        cardname_hashmap_for_start_turn[CardName.YURINA_GIYENBANJO] = 6
        cardname_hashmap_for_start_turn[CardName.YURINA_WOLYUNGNACK] = 7
        cardname_hashmap_for_start_turn[CardName.YURINA_POBARAM] = 8
        cardname_hashmap_for_start_turn[CardName.YURINA_JJOCKBAE] = 9
        cardname_hashmap_for_start_turn[CardName.YURINA_JURUCK] = 10

        cardname_hashmap_for_start_turn[CardName.SAINE_DOUBLEBEGI] = 100
        cardname_hashmap_for_start_turn[CardName.SAINE_HURUBEGI] = 101
        cardname_hashmap_for_start_turn[CardName.SAINE_MOOGECHOO] = 102
        cardname_hashmap_for_start_turn[CardName.SAINE_GANPA] = 103
        cardname_hashmap_for_start_turn[CardName.SAINE_GWONYUCK] = 104
        cardname_hashmap_for_start_turn[CardName.SAINE_CHOONGEMJUNG] = 105
        cardname_hashmap_for_start_turn[CardName.SAINE_MOOEMBUCK] = 106
        cardname_hashmap_for_start_turn[CardName.SAINE_YULDONGHOGEK] = 107
        cardname_hashmap_for_start_turn[CardName.SAINE_HANGMUNGGONGJIN] = 108
        cardname_hashmap_for_start_turn[CardName.SAINE_EMMOOSHOEBING] = 109
        cardname_hashmap_for_start_turn[CardName.SAINE_JONGGEK] = 110

        cardname_hashmap_for_start_turn[CardName.HIMIKA_SHOOT] = 200
        cardname_hashmap_for_start_turn[CardName.HIMIKA_RAPIDFIRE] = 201
        cardname_hashmap_for_start_turn[CardName.HIMIKA_MAGNUMCANON] = 202
        cardname_hashmap_for_start_turn[CardName.HIMIKA_FULLBURST] = 203
        cardname_hashmap_for_start_turn[CardName.HIMIKA_BACKSTEP] = 204
        cardname_hashmap_for_start_turn[CardName.HIMIKA_BACKDRAFT] = 205
        cardname_hashmap_for_start_turn[CardName.HIMIKA_SMOKE] = 206
        cardname_hashmap_for_start_turn[CardName.HIMIKA_REDBULLET] = 207
        cardname_hashmap_for_start_turn[CardName.HIMIKA_CRIMSONZERO] = 208
        cardname_hashmap_for_start_turn[CardName.HIMIKA_SCARLETIMAGINE] = 209
        cardname_hashmap_for_start_turn[CardName.HIMIKA_BURMILIONFIELD] = 210

        cardname_hashmap_for_start_turn[CardName.TOKOYO_BITSUNERIGI] = 300
        cardname_hashmap_for_start_turn[CardName.TOKOYO_WOOAHHANTAGUCK] = 301
        cardname_hashmap_for_start_turn[CardName.TOKOYO_RUNNINGRABIT] = 302
        cardname_hashmap_for_start_turn[CardName.TOKOYO_POETDANCE] = 303
        cardname_hashmap_for_start_turn[CardName.TOKOYO_FLIPFAN] = 304
        cardname_hashmap_for_start_turn[CardName.TOKOYO_WINDSTAGE] = 305
        cardname_hashmap_for_start_turn[CardName.TOKOYO_SUNSTAGE] = 306
        cardname_hashmap_for_start_turn[CardName.TOKOYO_KUON] = 307
        cardname_hashmap_for_start_turn[CardName.TOKOYO_THOUSANDBIRD] = 308
        cardname_hashmap_for_start_turn[CardName.TOKOYO_ENDLESSWIND] = 309
        cardname_hashmap_for_start_turn[CardName.TOKOYO_TOKOYOMOON] = 310

        //for second turn player 10000~19999
        cardname_hashmap_for_second_turn[CardName.YURINA_CHAM] = 10000
        cardname_hashmap_for_second_turn[CardName.YURINA_ILSUM] = 10001
        cardname_hashmap_for_second_turn[CardName.YURINA_JARUCHIGI] = 10002
        cardname_hashmap_for_second_turn[CardName.YURINA_GUHAB] = 10003
        cardname_hashmap_for_second_turn[CardName.YURINA_GIBACK] = 10004
        cardname_hashmap_for_second_turn[CardName.YURINA_APDO] = 10005
        cardname_hashmap_for_second_turn[CardName.YURINA_GIYENBANJO] = 10006
        cardname_hashmap_for_second_turn[CardName.YURINA_WOLYUNGNACK] = 10007
        cardname_hashmap_for_second_turn[CardName.YURINA_POBARAM] = 10008
        cardname_hashmap_for_second_turn[CardName.YURINA_JJOCKBAE] = 10009
        cardname_hashmap_for_second_turn[CardName.YURINA_JURUCK] = 10010

        cardname_hashmap_for_second_turn[CardName.SAINE_DOUBLEBEGI] = 10100
        cardname_hashmap_for_second_turn[CardName.SAINE_HURUBEGI] = 10101
        cardname_hashmap_for_second_turn[CardName.SAINE_MOOGECHOO] = 10102
        cardname_hashmap_for_second_turn[CardName.SAINE_GANPA] = 10103
        cardname_hashmap_for_second_turn[CardName.SAINE_GWONYUCK] = 10104
        cardname_hashmap_for_second_turn[CardName.SAINE_CHOONGEMJUNG] = 10105
        cardname_hashmap_for_second_turn[CardName.SAINE_MOOEMBUCK] = 10106
        cardname_hashmap_for_second_turn[CardName.SAINE_YULDONGHOGEK] = 10107
        cardname_hashmap_for_second_turn[CardName.SAINE_HANGMUNGGONGJIN] = 10108
        cardname_hashmap_for_second_turn[CardName.SAINE_EMMOOSHOEBING] = 10109
        cardname_hashmap_for_second_turn[CardName.SAINE_JONGGEK] = 10110

        cardname_hashmap_for_second_turn[CardName.HIMIKA_SHOOT] = 10200
        cardname_hashmap_for_second_turn[CardName.HIMIKA_RAPIDFIRE] = 10201
        cardname_hashmap_for_second_turn[CardName.HIMIKA_MAGNUMCANON] = 10202
        cardname_hashmap_for_second_turn[CardName.HIMIKA_FULLBURST] = 10203
        cardname_hashmap_for_second_turn[CardName.HIMIKA_BACKSTEP] = 10204
        cardname_hashmap_for_second_turn[CardName.HIMIKA_BACKDRAFT] = 10205
        cardname_hashmap_for_second_turn[CardName.HIMIKA_SMOKE] = 10206
        cardname_hashmap_for_second_turn[CardName.HIMIKA_REDBULLET] = 10207
        cardname_hashmap_for_second_turn[CardName.HIMIKA_CRIMSONZERO] = 10208
        cardname_hashmap_for_second_turn[CardName.HIMIKA_SCARLETIMAGINE] = 10209
        cardname_hashmap_for_second_turn[CardName.HIMIKA_BURMILIONFIELD] = 10210

        cardname_hashmap_for_second_turn[CardName.TOKOYO_BITSUNERIGI] = 10300
        cardname_hashmap_for_second_turn[CardName.TOKOYO_WOOAHHANTAGUCK] = 10301
        cardname_hashmap_for_second_turn[CardName.TOKOYO_RUNNINGRABIT] = 10302
        cardname_hashmap_for_second_turn[CardName.TOKOYO_POETDANCE] = 10303
        cardname_hashmap_for_second_turn[CardName.TOKOYO_FLIPFAN] = 10304
        cardname_hashmap_for_second_turn[CardName.TOKOYO_WINDSTAGE] = 10305
        cardname_hashmap_for_second_turn[CardName.TOKOYO_SUNSTAGE] = 10306
        cardname_hashmap_for_second_turn[CardName.TOKOYO_KUON] = 10307
        cardname_hashmap_for_second_turn[CardName.TOKOYO_THOUSANDBIRD] = 10308
        cardname_hashmap_for_second_turn[CardName.TOKOYO_ENDLESSWIND] = 10309
        cardname_hashmap_for_second_turn[CardName.TOKOYO_TOKOYOMOON] = 10310
    }

    fun init(){
        hashMapInit()

        YurinaCardInit()
    }


    fun returnCardDataByName(card_name: CardName): CardData {
        when (card_name){
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
            CardName.SAINE_DOUBLEBEGI -> TODO()
            CardName.SAINE_HURUBEGI -> TODO()
            CardName.SAINE_MOOGECHOO -> TODO()
            CardName.SAINE_GANPA -> TODO()
            CardName.SAINE_GWONYUCK -> TODO()
            CardName.SAINE_CHOONGEMJUNG -> TODO()
            CardName.SAINE_MOOEMBUCK -> TODO()
            CardName.SAINE_YULDONGHOGEK -> TODO()
            CardName.SAINE_HANGMUNGGONGJIN -> TODO()
            CardName.SAINE_EMMOOSHOEBING -> TODO()
            CardName.SAINE_JONGGEK -> TODO()
            CardName.HIMIKA_SHOOT -> TODO()
            CardName.HIMIKA_RAPIDFIRE -> TODO()
            CardName.HIMIKA_MAGNUMCANON -> TODO()
            CardName.HIMIKA_FULLBURST -> TODO()
            CardName.HIMIKA_BACKSTEP -> TODO()
            CardName.HIMIKA_BACKDRAFT -> TODO()
            CardName.HIMIKA_SMOKE -> TODO()
            CardName.HIMIKA_REDBULLET -> TODO()
            CardName.HIMIKA_CRIMSONZERO -> TODO()
            CardName.HIMIKA_SCARLETIMAGINE -> TODO()
            CardName.HIMIKA_BURMILIONFIELD -> TODO()
            CardName.TOKOYO_BITSUNERIGI -> TODO()
            CardName.TOKOYO_WOOAHHANTAGUCK -> TODO()
            CardName.TOKOYO_RUNNINGRABIT -> TODO()
            CardName.TOKOYO_POETDANCE -> TODO()
            CardName.TOKOYO_FLIPFAN -> TODO()
            CardName.TOKOYO_WINDSTAGE -> TODO()
            CardName.TOKOYO_SUNSTAGE -> TODO()
            CardName.TOKOYO_KUON -> TODO()
            CardName.TOKOYO_THOUSANDBIRD -> TODO()
            CardName.TOKOYO_ENDLESSWIND -> TODO()
            CardName.TOKOYO_TOKOYOMOON -> TODO()
            else -> return unused
        }
    }
}