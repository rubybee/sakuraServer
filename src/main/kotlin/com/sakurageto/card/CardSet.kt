package com.sakurageto.card

import com.sakurageto.gamelogic.GameStatus
import com.sakurageto.gamelogic.MegamiEnum

object CardSet {
    val cham = CardData(CardClass.NORMAL, CardName.YURINA_CHAM, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    val ilsom = CardData(CardClass.NORMAL, CardName.YURINA_ILSUM, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    val jaru_chigi = CardData(CardClass.NORMAL, CardName.YURINA_JARUCHIGI, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    val guhab = CardData(CardClass.NORMAL, CardName.YURINA_GUHAB, MegamiEnum.YURINA, CardType.ATTACK, SubType.FULLPOWER)
    val giback = CardData(CardClass.NORMAL, CardName.YURINA_GIBACK, MegamiEnum.YURINA, CardType.BEHAVIOR, SubType.NONE)
    val apdo = CardData(CardClass.NORMAL, CardName.YURINA_APDO, MegamiEnum.YURINA, CardType.ENCHANTMENT, SubType.NONE)
    val giyenbanzo = CardData(CardClass.NORMAL, CardName.YURINA_GIYENBANJO, MegamiEnum.YURINA, CardType.ENCHANTMENT, SubType.FULLPOWER)
    val wolyungnack = CardData(CardClass.SPECIAL, CardName.YURINA_WOLYUNGNACK, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    val jjockbae = CardData(CardClass.SPECIAL, CardName.YURINA_JJOCKBAE, MegamiEnum.YURINA, CardType.BEHAVIOR, SubType.NONE)

    fun YurinaCardInitialization(){
        cham.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 3, 1)
        ilsom.setAttack(DistanceType.CONTINUOUS, Pair(3, 3), null, 2, 2)
        ilsom.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { player, game_status->
            if (game_status.getPlayerLife(player) <= 3) {
                game_status.addThisTurnAttackBuff(player, AttackBuff(AttackBufTag.PLUS_MINUS_IMMEDIATE, {_, _ -> true}, {madeAttack ->
                    madeAttack.auraPlusMinus(1)
                }))
            }
        })
        jaru_chigi.setAttack(DistanceType.CONTINUOUS, Pair(1, 2), null, 2, 1)
        jaru_chigi.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { player, game_status ->
            if (game_status.getPlayerLife(player) <= 3) {
                game_status.addThisTurnAttackBuff(player, AttackBuff(AttackBufTag.PLUS_MINUS, {_, _ -> true}, {madeAttack ->
                    madeAttack.auraPlusMinus(1)
                }))
            }
        })
        guhab.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 4, 3)
        guhab.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { player, game_status ->
            game_status.addThisTurnAttackBuff(player, AttackBuff(AttackBufTag.PLUS_MINUS_IMMEDIATE, {_, _ -> true}, {madeAttack ->
                if(game_status.distance <= 2){
                    madeAttack.lifePlusMinus(-1); madeAttack.auraPlusMinus(-1)
                }
            }))
        })
        giback.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.CHANGE_CONCENTRATION) { player, game_status ->
            game_status.addConcentration(player, 1)
        })
        giback.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { player, game_status->
            game_status.addThisTurnRangeBuff(player, RangeBuff(RangeBufTag.ADD, {_, _ -> true}, {madeattack ->
                madeattack?.run{
                    addRange(1, true); madeattack.canNotReactNormal()
                }
            }))
        })
        apdo.setEnchantment(2)
        apdo.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.CHASM, null))
        apdo.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) {player, game_status ->
            game_status.addPreAttackZone(player, MadeAttack(DistanceType.CONTINUOUS, 999,  3, Pair(1, 4), null, MegamiEnum.YURINA))
        })
        giyenbanzo.setEnchantment(4)
        giyenbanzo.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){ player, game_status ->
            game_status.addThisTurnAttackBuff(player, AttackBuff(AttackBufTag.PLUS_MINUS_IMMEDIATE, {_, _ -> true}, {madeAttack ->
                if(madeAttack.megami != MegamiEnum.YURINA) madeAttack.run {
                    Chogek(); auraPlusMinus(1); lifePlusMinus(1)
                }
            }))
        })
        wolyungnack.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 3, 4)
        wolyungnack.setSpecial(7)
        jjockbae.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.MOVE_SAKURA_TOKEN){ player, game_status ->
            game_status.dustToAura(player, 5)
        })
        TODO("RETURN JJOCKBAE")

    }

    fun cardInitialization(){
        YurinaCardInitialization()
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
            CardName.YURINA_POBARAM -> TODO()
            CardName.YURINA_JJOCKBAE -> TODO()
            CardName.YURINA_JURUCK -> TODO()
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
        }
    }
}