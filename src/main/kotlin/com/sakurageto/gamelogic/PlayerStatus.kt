package com.sakurageto.gamelogic

import com.sakurageto.card.*
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.SakuraSendData
import java.util.NoSuchElementException

class PlayerStatus {
    var full_action = false

    var max_hand = 2
    var max_aura = 5
    var aura = 3

    var using_card = ArrayDeque<Card>()

    var hand: MutableList<Card> = mutableListOf()

    fun getCardFromHand(card_name: CardName): Card?{
        for(card in hand){
            if(card.card_data.card_name == card_name){
                return card
            }
        }
        return null
    }

    fun useCardFromHand(card_name: CardName) {
        for(i in hand.indices){
            if(hand[i].card_data.card_name == card_name){
                using_card.addLast(hand[i])
                hand.removeAt(i)
                return
            }
        }
    }

    fun fromHandToCover(card_name: CardName): Boolean {
        for(i in hand.indices){
            if(hand[i].card_data.card_name == card_name){
                cover_card.addLast(hand[i])
                hand.removeAt(i)
                return true
            }
        }
        return false
    }

    var enchantment_card: HashMap<CardName, Card> = HashMap()

    var special_card_deck: ArrayDeque<Card> = ArrayDeque<Card>()

    fun getCardFromSpecial(card_name: CardName): Card?{
        for(card in special_card_deck){
            if(card.card_data.card_name == card_name){
                return card
            }
        }
        return null
    }

    fun useCardFromSpecial(card_name: CardName) {
        for(i in special_card_deck.indices){
            val card = special_card_deck.first()
            special_card_deck.removeFirst()
            if(card.card_data.card_name == card_name){
                using_card.addLast(card)
                return
            }
            special_card_deck.addLast(card)
        }
    }

    var normal_card_deck = ArrayDeque<Card>()
    var used_special_card = ArrayDeque<Card>()

    var discard = ArrayDeque<Card>()
    var cover_card = ArrayDeque<Card>()

    var end_turn = false

    fun usedToSpecial(card_name: CardName): Boolean{
        for(i in 0..used_special_card.size){
            val now = used_special_card.first()
            used_special_card.removeFirst()
            if(now.card_data.card_name == card_name){
                special_card_deck.addLast(now)
                return true
            }
            used_special_card.addLast(now)
        }
        return false
    }

    //return using dust
    fun plusAura(number: Int): Int{
        if(max_aura > aura + number){
            val temp = aura
            aura = max_aura
            return max_aura - temp
        }
        else{
            aura += number
            return number
        }
    }

    var life = 10
    var flare = 0

    var concentration = 0
    var max_concentration = 2
    var shrink = false

    //0 success add conentration, 1 fail because shrink, 2 can not plus because full
    fun addConcentration(): Int{
        if(shrink){
            shrink = false
            return 1
        }
        else if(concentration < max_concentration){
            concentration += 1
            return 0
        }
        return 2
    }

    fun decreaseConcentration(): Boolean{
        if(concentration == 0) return false
        concentration -= 1
        return true
    }


    lateinit var megami_1: MegamiEnum
    lateinit var megami_2: MegamiEnum
    lateinit var megami_ban: MegamiEnum

    var unselected_card: MutableList<CardName> = mutableListOf()
    var unselected_specialcard: MutableList<CardName> = mutableListOf()

    var pre_attack_card: MadeAttack? = null

    fun addPreAttackZone(madeAttack: MadeAttack){
        pre_attack_card = madeAttack
    }

    var attack_buf: Array<ArrayDeque<Buff>> = arrayOf(
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque()
    )

    var range_buf: Array<ArrayDeque<RangeBuff>> = arrayOf(
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
    )

    var cost_buf: Array<ArrayDeque<CostBuff>> = arrayOf(
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque()
    )

    fun addAttackBuff(buf: Buff){
        when(buf.tag){
            BufTag.INSERT -> attack_buf[1].add(buf)
            BufTag.CHANGE_EACH -> attack_buf[3].add(buf)
            BufTag.MULTIPLE -> attack_buf[5].add(buf)
            BufTag.DIVIDE -> attack_buf[7].add(buf)
            BufTag.PLUS_MINUS -> attack_buf[9].add(buf)
            BufTag.INSERT_IMMEDIATE -> attack_buf[0].add(buf)
            BufTag.CHANGE_EACH_IMMEDIATE -> attack_buf[2].add(buf)
            BufTag.MULTIPLE_IMMEDIATE -> attack_buf[4].add(buf)
            BufTag.DIVIDE_IMMEDIATE -> attack_buf[6].add(buf)
            BufTag.PLUS_MINUS_IMMEDIATE -> attack_buf[8].add(buf)
            else -> attack_buf[11].add(buf)
        }
    }

    fun addRangeBuff(buf: RangeBuff){
        when(buf.tag){
            RangeBufTag.CHANGE -> range_buf[1].add(buf)
            RangeBufTag.ADD -> range_buf[3].add(buf)
            RangeBufTag.DELETE -> range_buf[5].add(buf)
            RangeBufTag.PLUS -> range_buf[7].add(buf)
            RangeBufTag.MINUS -> range_buf[9].add(buf)
            RangeBufTag.CHANGE_IMMEDIATE -> range_buf[0].add(buf)
            RangeBufTag.ADD_IMMEDIATE -> range_buf[2].add(buf)
            RangeBufTag.DELETE_IMMEDIATE -> range_buf[4].add(buf)
            RangeBufTag.PLUS_IMMEDIATE -> range_buf[6].add(buf)
            RangeBufTag.MINUS_IMMEDIATE -> range_buf[8].add(buf)
        }
    }

    fun addCostBuff(buf: CostBuff){
        when(buf.tag){
            BufTag.INSERT -> cost_buf[1].add(buf)
            BufTag.CHANGE_EACH -> cost_buf[3].add(buf)
            BufTag.MULTIPLE -> cost_buf[5].add(buf)
            BufTag.DIVIDE -> cost_buf[7].add(buf)
            BufTag.PLUS_MINUS -> cost_buf[9].add(buf)
            BufTag.INSERT_IMMEDIATE -> cost_buf[0].add(buf)
            BufTag.CHANGE_EACH_IMMEDIATE -> cost_buf[2].add(buf)
            BufTag.MULTIPLE_IMMEDIATE -> cost_buf[4].add(buf)
            BufTag.DIVIDE_IMMEDIATE -> cost_buf[6].add(buf)
            BufTag.PLUS_MINUS_IMMEDIATE -> cost_buf[8].add(buf)
            else -> cost_buf[11].add(buf)
        }
    }

    fun setMegamiSSangjang(data: SakuraSendData){
        megami_1 = try{
            MegamiEnum.fromInt(data.data?.get(0)?: 10)
        }catch (e: NoSuchElementException){
            MegamiEnum.fromInt(10)
        }

        megami_2 = try{
            MegamiEnum.fromInt(data.data?.get(1)?: 20)
        }catch (e: NoSuchElementException){
            MegamiEnum.fromInt(20)
        }

        if(megami_1 == megami_2){
            megami_1 = MegamiEnum.YURINA
            megami_2 = MegamiEnum.HIMIKA
        }
    }

    fun setMegamiSamSep(data: SakuraSendData){
        megami_1 = try{
            MegamiEnum.fromInt(data.data?.get(0)?: 10)
        }catch (e: NoSuchElementException){
            MegamiEnum.fromInt(10)
        }

        megami_2 = try{
            MegamiEnum.fromInt(data.data?.get(1)?: 20)
        }catch (e: NoSuchElementException){
            MegamiEnum.fromInt(20)
        }

        megami_ban = try{
            MegamiEnum.fromInt(data.data?.get(2)?: 30)
        }catch (e: NoSuchElementException){
            MegamiEnum.fromInt(30)
        }

        if(megami_2 == megami_1){
            megami_1 = MegamiEnum.YURINA
            megami_2 = MegamiEnum.HIMIKA
            megami_ban = MegamiEnum.SAINE
        }
        else if(megami_1 == megami_ban){
            megami_1 = MegamiEnum.YURINA
            megami_2 = MegamiEnum.HIMIKA
            megami_ban = MegamiEnum.SAINE
        }
        else if(megami_2 == megami_ban){
            megami_1 = MegamiEnum.YURINA
            megami_2 = MegamiEnum.HIMIKA
            megami_ban = MegamiEnum.SAINE
        }
    }

    fun returnListMegami2(): MutableList<Int>{
        return mutableListOf(megami_1.real_number, megami_2.real_number)
    }

    fun returnListMegami3(): MutableList<Int>{
        return mutableListOf(megami_1.real_number, megami_2.real_number, megami_ban.real_number)
    }

    fun banMegami(data: SakuraSendData){
        val ben_megami = data.data?.get(0)?: megami_1
        if (ben_megami != megami_ban.real_number){
            if(ben_megami == megami_1.real_number){
                megami_1 = megami_ban
            }
            else{
                megami_2 = megami_ban
            }
        }
    }

    fun makeMegamiData(command: CommandEnum): SakuraSendData {
        return SakuraSendData(command, mutableListOf(megami_1.real_number, megami_2.real_number))
    }

    fun deleteNormalUsedCard(card: MutableList<CardName>){
        for(name in card){
            val now = unselected_card.indexOf(name)
            if(now != -1){
                unselected_card.removeAt(now)
            }
        }
    }

    fun deleteSpeicalUsedCard(card: MutableList<CardName>){
        for(name in card){
            val now = unselected_card.indexOf(name)
            if(now != -1){
                unselected_specialcard.removeAt(now)
            }
        }
    }
}