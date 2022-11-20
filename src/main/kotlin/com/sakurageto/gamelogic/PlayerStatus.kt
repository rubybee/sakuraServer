package com.sakurageto.gamelogic

import com.sakurageto.card.*
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.SakuraSendData
import java.util.LinkedList
import java.util.Queue
import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf.Effect

class PlayerStatus {
    var max_aura = 5
    var aura = 3

    var hand: MutableList<Card> = mutableListOf()
    var enchantment_card: HashMap<CardName, Card> = HashMap()

    var special_card_deck: ArrayDeque<Card> = ArrayDeque<Card>()
    var normal_card_deck: ArrayDeque<Card> = ArrayDeque<Card>()
    var used_special_card: ArrayDeque<Card> = ArrayDeque<Card>()


    fun usedToSpecial(card_name: CardName): Boolean{
        for(i in 0..used_special_card.size){
            val now = used_special_card.first()
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



    lateinit var megami_1: MegamiEnum
    lateinit var megami_2: MegamiEnum
    lateinit var megami_ban: MegamiEnum

    var unselected_card: MutableList<CardName> = mutableListOf()
    var unselected_specialcard: MutableList<CardName> = mutableListOf()

    lateinit var pre_attack_card: MadeAttack

    fun addPreAttackZone(madeAttack: MadeAttack){
        pre_attack_card = madeAttack
    }

    var attack_buf: Array<ArrayDeque<AttackBuff>> = arrayOf(
        ArrayDeque<AttackBuff>(),
        ArrayDeque<AttackBuff>(),
        ArrayDeque<AttackBuff>(),
        ArrayDeque<AttackBuff>(),
        ArrayDeque<AttackBuff>(),
        ArrayDeque<AttackBuff>(),
        ArrayDeque<AttackBuff>(),
        ArrayDeque<AttackBuff>(),
        ArrayDeque<AttackBuff>(),
        ArrayDeque<AttackBuff>(),
        ArrayDeque<AttackBuff>(),
    )
    var range_buf: Array<ArrayDeque<RangeBuff>> = arrayOf(
        ArrayDeque<RangeBuff>(),
        ArrayDeque<RangeBuff>(),
        ArrayDeque<RangeBuff>(),
        ArrayDeque<RangeBuff>(),
        ArrayDeque<RangeBuff>(),
        ArrayDeque<RangeBuff>(),
        ArrayDeque<RangeBuff>(),
        ArrayDeque<RangeBuff>(),
        ArrayDeque<RangeBuff>(),
        ArrayDeque<RangeBuff>(),
    )

    fun addAttackBuff(buf: AttackBuff){
        when(buf.tag){
            AttackBufTag.INSERT -> attack_buf[1].add(buf)
            AttackBufTag.CHANGE_EACH -> attack_buf[3].add(buf)
            AttackBufTag.MULTIPLE -> attack_buf[5].add(buf)
            AttackBufTag.DIVIDE -> attack_buf[7].add(buf)
            AttackBufTag.PLUS_MINUS -> attack_buf[9].add(buf)
            AttackBufTag.INSERT_IMMEDIATE -> attack_buf[2].add(buf)
            AttackBufTag.CHANGE_EACH_IMMEDIATE -> attack_buf[4].add(buf)
            AttackBufTag.MULTIPLE_IMMEDIATE -> attack_buf[6].add(buf)
            AttackBufTag.DIVIDE_IMMEDIATE -> attack_buf[8].add(buf)
            AttackBufTag.PLUS_MINUS_IMMEDIATE -> attack_buf[10].add(buf)
            else -> attack_buf[0].add(buf)
        }
    }

    fun addRangeBuff(buf: RangeBuff){
        when(buf.tag){
            RangeBufTag.CHANGE -> range_buf[0].add(buf)
            RangeBufTag.ADD -> range_buf[2].add(buf)
            RangeBufTag.DELETE -> range_buf[4].add(buf)
            RangeBufTag.PLUS -> range_buf[6].add(buf)
            RangeBufTag.MINUS -> range_buf[8].add(buf)
            RangeBufTag.CHANGE_IMMEDIATE -> range_buf[1].add(buf)
            RangeBufTag.ADD_IMMEDIATE -> range_buf[3].add(buf)
            RangeBufTag.DELETE_IMMEDIATE -> range_buf[5].add(buf)
            RangeBufTag.PLUS_IMMEDIATE -> range_buf[7].add(buf)
            RangeBufTag.MINUS_IMMEDIATE -> range_buf[9].add(buf)
        }
    }

    fun setMegamiSSangjang(data: SakuraSendData){
        megami_1 = MegamiEnum.fromInt(data.data?.get(0)!!)
        megami_2 = MegamiEnum.fromInt(data.data?.get(1)!!)
        if(megami_1 == megami_2){
            megami_1 = MegamiEnum.YURINA
            megami_2 = MegamiEnum.HIMIKA
        }
    }

    fun setMegamiSamSep(data: SakuraSendData){
        megami_1 = MegamiEnum.fromInt(data.data?.get(0)!!)
        megami_2 = MegamiEnum.fromInt(data.data?.get(1)!!)
        megami_ban = MegamiEnum.fromInt(data.data?.get(2)!!)
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
        val ben_megami = data.data?.get(0)!!
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
}