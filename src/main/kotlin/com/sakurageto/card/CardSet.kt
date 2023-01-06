package com.sakurageto.card

import com.sakurageto.gamelogic.GameStatus
import com.sakurageto.gamelogic.ImmediateBackListner
import com.sakurageto.gamelogic.MegamiEnum
import com.sakurageto.gamelogic.PlayerStatus
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum

object CardSet {
    val cardNameHashmapFirst = HashMap<CardName, Int>()
    val cardNameHashmapSecond = HashMap<CardName, Int>()
    val cardNumberHashmap = HashMap<CardName, Int>()

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
        ilsom.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { player, game_status, _->
            if (gulSa(player, game_status)) {
                game_status.addThisTurnAttackBuff(player, Buff(CardName.YURINA_ILSUM, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _ -> true}, {madeAttack ->
                    madeAttack.auraPlusMinus(1)
                }))
            }
            null
        })
        jaru_chigi.setAttack(DistanceType.CONTINUOUS, Pair(1, 2), null, 2, 1)
        jaru_chigi.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { player, game_status, _ ->
            if (gulSa(player, game_status)) {
                game_status.addThisTurnAttackBuff(player, Buff(CardName.YURINA_JARUCHIGI, 1, BufTag.PLUS_MINUS, {_, _ -> true}, {madeAttack ->
                    madeAttack.auraPlusMinus(1)
                }))
            }
            null
        })
        guhab.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 4, 3)
        guhab.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(CardName.YURINA_GUHAB, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _ -> true}, {madeAttack ->
                if(game_status.thisTurnDistance <= 2){
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
            game_status.addThisTurnRangeBuff(player, RangeBuff(CardName.YURINA_GIBACK,1, RangeBufTag.ADD, {_, _ -> true}, {attack ->
                attack.run{
                    addRange(1, true); canNotReactNormal()
                }
            }))
            null
        })
        apdo.setEnchantment(2)
        apdo.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHASM, null))
        apdo.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) {player, game_status, _ ->
            game_status.addPreAttackZone(player, MadeAttack(DistanceType.CONTINUOUS, 3,  999, Pair(1, 4), null, MegamiEnum.YURINA))
            null
        })
        giyenbanzo.setEnchantment(4)
        giyenbanzo.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){ player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(CardName.YURINA_GIYENBANJO, 1, BufTag.PLUS_MINUS_IMMEDIATE, { _, _ -> true}, { madeAttack ->
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
        pobaram.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_REDUCE){ _, _, reactedAttack ->
            reactedAttack?.auraPlusMinus(-2)
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
            if(gulSa(player, game_status)) 1
            else 0
        })
    }

    private val doublebegi = CardData(CardClass.NORMAL, CardName.SAINE_DOUBLEBEGI, MegamiEnum.SAINE, CardType.ATTACK, SubType.NONE)
    private val hurubegi = CardData(CardClass.NORMAL, CardName.SAINE_HURUBEGI, MegamiEnum.SAINE, CardType.ATTACK, SubType.REACTION)
    private val moogechoo = CardData(CardClass.NORMAL, CardName.SAINE_MOOGECHOO, MegamiEnum.SAINE, CardType.ATTACK, SubType.REACTION)
    private val ganpa = CardData(CardClass.NORMAL, CardName.SAINE_GANPA, MegamiEnum.SAINE, CardType.BEHAVIOR, SubType.NONE)
    private val gwonyuck = CardData(CardClass.NORMAL, CardName.SAINE_GWONYUCK, MegamiEnum.SAINE, CardType.ENCHANTMENT, SubType.NONE)
    private fun palSang(player: PlayerEnum, game_status: GameStatus): Boolean{
        return game_status.getPlayerAura(player) <= 1
    }
    private fun saineCardInit(){
        doublebegi.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 2, 1)
        doublebegi.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_ATTACK) {player, game_status, _ ->
            if(palSang(player, game_status)){
                game_status.addPreAttackZone(player, MadeAttack(DistanceType.CONTINUOUS, 2,  1, Pair(4, 5), null, MegamiEnum.SAINE))
            }
            null
        })
        hurubegi.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 3, 1)
        moogechoo.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 2, 1)
        moogechoo.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {player, game_status, _ ->
            if(palSang(player, game_status)){
                game_status.dustToDistance(1)
            }
            null
        })
        ganpa.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.CAN_REACTABLE) {player, game_status, _ ->
            if(palSang(player, game_status)) 1
            else 0
        })
        ganpa.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK) {player, game_status, _ ->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player)
                if(nowCommand == CommandEnum.SELECT_DUST_TO_DISTANCE){
                    game_status.dustToDistance(1)
                    break
                }
                else if(nowCommand == CommandEnum.SELECT_DISTANCE_TO_DUST){
                    game_status.distanceToDust(1)
                    break
                }
            }
            null
        })
        gwonyuck.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.THIS_CARD_NAP_LOCATION_CHANGE) {_, _, _ ->
            LocationEnum.DISTANCE.real_number
        })
        gwonyuck.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHANGE_SWELL_DISTANCE) {_, _, _ ->
            1
        })
    }

    fun init(){
        hashMapInit()

        yurinaCardInit()
        saineCardInit()
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
            CardName.SAINE_DOUBLEBEGI -> return doublebegi
            CardName.SAINE_HURUBEGI -> return hurubegi
            CardName.SAINE_MOOGECHOO -> return moogechoo
            CardName.SAINE_GANPA -> return ganpa
            CardName.SAINE_GWONYUCK -> return gwonyuck
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