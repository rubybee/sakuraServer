package com.sakurageto.card

import com.sakurageto.gamelogic.*
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import com.sakurageto.protocol.receiveSelectCard
import kotlin.math.abs

object CardSet {
    val cardNameHashmapFirst = HashMap<CardName, Int>()
    val cardNameHashmapSecond = HashMap<CardName, Int>()
    val cardNumberHashmap = HashMap<Int, CardName>()

    private const val SHINRA_SHINRA_CARD_NUMBER = 100001

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

        cardNameHashmapFirst[CardName.SHINRA_SHINRA] = SHINRA_SHINRA_CARD_NUMBER
        cardNameHashmapFirst[CardName.SHINRA_IBLON] = 700
        cardNameHashmapFirst[CardName.SHINRA_BANLON] = 701
        cardNameHashmapFirst[CardName.SHINRA_KIBEN] = 702
        cardNameHashmapFirst[CardName.SHINRA_INYONG] = 703
        cardNameHashmapFirst[CardName.SHINRA_SEONDONG] = 704
        cardNameHashmapFirst[CardName.SHINRA_JANGDAM] = 705
        cardNameHashmapFirst[CardName.SHINRA_NONPA] = 706
        cardNameHashmapFirst[CardName.SHINRA_WANJEON_NONPA] = 707
        cardNameHashmapFirst[CardName.SHINRA_DASIG_IHAE] = 708
        cardNameHashmapFirst[CardName.SHINRA_CHEONJI_BANBAG] = 709
        cardNameHashmapFirst[CardName.SHINRA_SAMRA_BAN_SHO] = 710

        cardNameHashmapFirst[CardName.HAGANE_CENTRIFUGAL_ATTACK] = 800
        cardNameHashmapFirst[CardName.HAGANE_FOUR_WINDED_EARTHQUAKE] = 801
        cardNameHashmapFirst[CardName.HAGANE_GROUND_BREAKING] = 802
        cardNameHashmapFirst[CardName.HAGANE_HYPER_RECOIL] = 803
        cardNameHashmapFirst[CardName.HAGANE_WON_MU_RUYN] = 804
        cardNameHashmapFirst[CardName.HAGANE_RING_A_BELL] = 805
        cardNameHashmapFirst[CardName.HAGANE_GRAVITATION_FIELD] = 806
        cardNameHashmapFirst[CardName.HAGANE_GRAND_SKY_HOLE_CRASH] = 807
        cardNameHashmapFirst[CardName.HAGANE_GRAND_BELL_MEGALOBEL] = 808
        cardNameHashmapFirst[CardName.HAGANE_GRAND_GRAVITATION_ATTRACT] = 809
        cardNameHashmapFirst[CardName.HAGANE_GRAND_MOUNTAIN_RESPECT] = 810

        cardNameHashmapFirst[CardName.CHIKAGE_THROW_KUNAI] = 900
        cardNameHashmapFirst[CardName.CHIKAGE_POISON_NEEDLE] = 901
        cardNameHashmapFirst[CardName.CHIKAGE_TO_ZU_CHU] = 902
        cardNameHashmapFirst[CardName.CHIKAGE_CUTTING_NECK] = 903
        cardNameHashmapFirst[CardName.CHIKAGE_POISON_SMOKE] = 904
        cardNameHashmapFirst[CardName.CHIKAGE_TIP_TOEING] = 905
        cardNameHashmapFirst[CardName.CHIKAGE_MUDDLE] = 906
        cardNameHashmapFirst[CardName.CHIKAGE_DEADLY_POISON] = 907
        cardNameHashmapFirst[CardName.CHIKAGE_HAN_KI_POISON] = 908
        cardNameHashmapFirst[CardName.CHIKAGE_REINCARNATION_POISON] = 909
        cardNameHashmapFirst[CardName.CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIFE] = 910
        cardNameHashmapFirst[CardName.POISON_PARALYTIC] = 995
        cardNameHashmapFirst[CardName.POISON_HALLUCINOGENIC] = 996
        cardNameHashmapFirst[CardName.POISON_RELAXATION] = 997
        cardNameHashmapFirst[CardName.POISON_DEADLY_1] = 998
        cardNameHashmapFirst[CardName.POISON_DEADLY_2] = 999

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

        cardNameHashmapSecond[CardName.SHINRA_SHINRA] = SHINRA_SHINRA_CARD_NUMBER
        cardNameHashmapSecond[CardName.SHINRA_IBLON] = 10700
        cardNameHashmapSecond[CardName.SHINRA_BANLON] = 10701
        cardNameHashmapSecond[CardName.SHINRA_KIBEN] = 10702
        cardNameHashmapSecond[CardName.SHINRA_INYONG] = 10703
        cardNameHashmapSecond[CardName.SHINRA_SEONDONG] = 10704
        cardNameHashmapSecond[CardName.SHINRA_JANGDAM] = 10705
        cardNameHashmapSecond[CardName.SHINRA_NONPA] = 10706
        cardNameHashmapSecond[CardName.SHINRA_WANJEON_NONPA] = 10707
        cardNameHashmapSecond[CardName.SHINRA_DASIG_IHAE] = 10708
        cardNameHashmapSecond[CardName.SHINRA_CHEONJI_BANBAG] = 10709
        cardNameHashmapSecond[CardName.SHINRA_SAMRA_BAN_SHO] = 10710

        cardNameHashmapSecond[CardName.HAGANE_CENTRIFUGAL_ATTACK] = 10800
        cardNameHashmapSecond[CardName.HAGANE_FOUR_WINDED_EARTHQUAKE] = 10801
        cardNameHashmapSecond[CardName.HAGANE_GROUND_BREAKING] = 10802
        cardNameHashmapSecond[CardName.HAGANE_HYPER_RECOIL] = 10803
        cardNameHashmapSecond[CardName.HAGANE_WON_MU_RUYN] = 10804
        cardNameHashmapSecond[CardName.HAGANE_RING_A_BELL] = 10805
        cardNameHashmapSecond[CardName.HAGANE_GRAVITATION_FIELD] = 10806
        cardNameHashmapSecond[CardName.HAGANE_GRAND_SKY_HOLE_CRASH] = 10807
        cardNameHashmapSecond[CardName.HAGANE_GRAND_BELL_MEGALOBEL] = 10808
        cardNameHashmapSecond[CardName.HAGANE_GRAND_GRAVITATION_ATTRACT] = 10809
        cardNameHashmapSecond[CardName.HAGANE_GRAND_MOUNTAIN_RESPECT] = 10810

        cardNameHashmapSecond[CardName.CHIKAGE_THROW_KUNAI] = 10900
        cardNameHashmapSecond[CardName.CHIKAGE_POISON_NEEDLE] = 10901
        cardNameHashmapSecond[CardName.CHIKAGE_TO_ZU_CHU] = 10902
        cardNameHashmapSecond[CardName.CHIKAGE_CUTTING_NECK] = 10903
        cardNameHashmapSecond[CardName.CHIKAGE_POISON_SMOKE] = 10904
        cardNameHashmapSecond[CardName.CHIKAGE_TIP_TOEING] = 10905
        cardNameHashmapSecond[CardName.CHIKAGE_MUDDLE] = 10906
        cardNameHashmapSecond[CardName.CHIKAGE_DEADLY_POISON] = 10907
        cardNameHashmapSecond[CardName.CHIKAGE_HAN_KI_POISON] = 10908
        cardNameHashmapSecond[CardName.CHIKAGE_REINCARNATION_POISON] = 10909
        cardNameHashmapSecond[CardName.CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIFE] = 10910
        cardNameHashmapSecond[CardName.POISON_PARALYTIC] = 10995
        cardNameHashmapSecond[CardName.POISON_HALLUCINOGENIC] = 10996
        cardNameHashmapSecond[CardName.POISON_RELAXATION] = 10997
        cardNameHashmapSecond[CardName.POISON_DEADLY_1] = 10998
        cardNameHashmapSecond[CardName.POISON_DEADLY_2] = 10999

        //for number -> card name
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

        cardNumberHashmap[SHINRA_SHINRA_CARD_NUMBER] = CardName.SHINRA_SHINRA
        cardNumberHashmap[701] = CardName.SHINRA_BANLON
        cardNumberHashmap[702] = CardName.SHINRA_KIBEN
        cardNumberHashmap[703] = CardName.SHINRA_INYONG
        cardNumberHashmap[704] = CardName.SHINRA_SEONDONG
        cardNumberHashmap[705] = CardName.SHINRA_JANGDAM
        cardNumberHashmap[706] = CardName.SHINRA_NONPA
        cardNumberHashmap[707] = CardName.SHINRA_WANJEON_NONPA
        cardNumberHashmap[708] = CardName.SHINRA_DASIG_IHAE
        cardNumberHashmap[709] = CardName.SHINRA_CHEONJI_BANBAG
        cardNumberHashmap[710] = CardName.SHINRA_SAMRA_BAN_SHO

        cardNumberHashmap[800] = CardName.HAGANE_CENTRIFUGAL_ATTACK
        cardNumberHashmap[801] = CardName.HAGANE_FOUR_WINDED_EARTHQUAKE
        cardNumberHashmap[802] = CardName.HAGANE_GROUND_BREAKING
        cardNumberHashmap[803] = CardName.HAGANE_HYPER_RECOIL
        cardNumberHashmap[804] = CardName.HAGANE_WON_MU_RUYN
        cardNumberHashmap[805] = CardName.HAGANE_RING_A_BELL
        cardNumberHashmap[806] = CardName.HAGANE_GRAVITATION_FIELD
        cardNumberHashmap[807] = CardName.HAGANE_GRAND_SKY_HOLE_CRASH
        cardNumberHashmap[808] = CardName.HAGANE_GRAND_BELL_MEGALOBEL
        cardNumberHashmap[809] = CardName.HAGANE_GRAND_GRAVITATION_ATTRACT
        cardNumberHashmap[810] = CardName.HAGANE_GRAND_MOUNTAIN_RESPECT

        cardNumberHashmap[900] = CardName.CHIKAGE_THROW_KUNAI
        cardNumberHashmap[901] = CardName.CHIKAGE_POISON_NEEDLE
        cardNumberHashmap[902] = CardName.CHIKAGE_TO_ZU_CHU
        cardNumberHashmap[903] = CardName.CHIKAGE_CUTTING_NECK
        cardNumberHashmap[904] = CardName.CHIKAGE_POISON_SMOKE
        cardNumberHashmap[905] = CardName.CHIKAGE_TIP_TOEING
        cardNumberHashmap[906] = CardName.CHIKAGE_MUDDLE
        cardNumberHashmap[907] = CardName.CHIKAGE_DEADLY_POISON
        cardNumberHashmap[908] = CardName.CHIKAGE_HAN_KI_POISON
        cardNumberHashmap[909] = CardName.CHIKAGE_REINCARNATION_POISON
        cardNumberHashmap[910] = CardName.CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIFE
        cardNumberHashmap[995] = CardName.POISON_PARALYTIC
        cardNumberHashmap[996] = CardName.POISON_HALLUCINOGENIC
        cardNumberHashmap[997] = CardName.POISON_RELAXATION
        cardNumberHashmap[998] = CardName.POISON_DEADLY_1
        cardNumberHashmap[999] = CardName.POISON_DEADLY_2

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

        cardNumberHashmap[SHINRA_SHINRA_CARD_NUMBER] = CardName.SHINRA_SHINRA
        cardNumberHashmap[10700] = CardName.SHINRA_IBLON
        cardNumberHashmap[10701] = CardName.SHINRA_BANLON
        cardNumberHashmap[10702] = CardName.SHINRA_KIBEN
        cardNumberHashmap[10703] = CardName.SHINRA_INYONG
        cardNumberHashmap[10704] = CardName.SHINRA_SEONDONG
        cardNumberHashmap[10705] = CardName.SHINRA_JANGDAM
        cardNumberHashmap[10706] = CardName.SHINRA_NONPA
        cardNumberHashmap[10707] = CardName.SHINRA_WANJEON_NONPA
        cardNumberHashmap[10708] = CardName.SHINRA_DASIG_IHAE
        cardNumberHashmap[10709] = CardName.SHINRA_CHEONJI_BANBAG
        cardNumberHashmap[10710] = CardName.SHINRA_SAMRA_BAN_SHO

        cardNumberHashmap[10800] = CardName.HAGANE_CENTRIFUGAL_ATTACK
        cardNumberHashmap[10801] = CardName.HAGANE_FOUR_WINDED_EARTHQUAKE
        cardNumberHashmap[10802] = CardName.HAGANE_GROUND_BREAKING
        cardNumberHashmap[10803] = CardName.HAGANE_HYPER_RECOIL
        cardNumberHashmap[10804] = CardName.HAGANE_WON_MU_RUYN
        cardNumberHashmap[10805] = CardName.HAGANE_RING_A_BELL
        cardNumberHashmap[10806] = CardName.HAGANE_GRAVITATION_FIELD
        cardNumberHashmap[10807] = CardName.HAGANE_GRAND_SKY_HOLE_CRASH
        cardNumberHashmap[10808] = CardName.HAGANE_GRAND_BELL_MEGALOBEL
        cardNumberHashmap[10809] = CardName.HAGANE_GRAND_GRAVITATION_ATTRACT
        cardNumberHashmap[10810] = CardName.HAGANE_GRAND_MOUNTAIN_RESPECT

        cardNumberHashmap[10900] = CardName.CHIKAGE_THROW_KUNAI
        cardNumberHashmap[10901] = CardName.CHIKAGE_POISON_NEEDLE
        cardNumberHashmap[10902] = CardName.CHIKAGE_TO_ZU_CHU
        cardNumberHashmap[10903] = CardName.CHIKAGE_CUTTING_NECK
        cardNumberHashmap[10904] = CardName.CHIKAGE_POISON_SMOKE
        cardNumberHashmap[10905] = CardName.CHIKAGE_TIP_TOEING
        cardNumberHashmap[10906] = CardName.CHIKAGE_MUDDLE
        cardNumberHashmap[10907] = CardName.CHIKAGE_DEADLY_POISON
        cardNumberHashmap[10908] = CardName.CHIKAGE_HAN_KI_POISON
        cardNumberHashmap[10909] = CardName.CHIKAGE_REINCARNATION_POISON
        cardNumberHashmap[10910] = CardName.CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIFE
        cardNumberHashmap[10995] = CardName.POISON_PARALYTIC
        cardNumberHashmap[10996] = CardName.POISON_HALLUCINOGENIC
        cardNumberHashmap[10997] = CardName.POISON_RELAXATION
        cardNumberHashmap[10998] = CardName.POISON_DEADLY_1
        cardNumberHashmap[10999] = CardName.POISON_DEADLY_2
    }

    private suspend fun selectDustToDistance(nowCommand: CommandEnum, game_status: GameStatus): Boolean{
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
    private val guhab = CardData(CardClass.NORMAL, CardName.YURINA_GUHAB, MegamiEnum.YURINA, CardType.ATTACK, SubType.FULL_POWER)
    private val giback = CardData(CardClass.NORMAL, CardName.YURINA_GIBACK, MegamiEnum.YURINA, CardType.BEHAVIOR, SubType.NONE)
    private val apdo = CardData(CardClass.NORMAL, CardName.YURINA_APDO, MegamiEnum.YURINA, CardType.ENCHANTMENT, SubType.NONE)
    private val giyenbanzo = CardData(CardClass.NORMAL, CardName.YURINA_GIYENBANJO, MegamiEnum.YURINA, CardType.ENCHANTMENT, SubType.FULL_POWER)
    private val wolyungnack = CardData(CardClass.SPECIAL, CardName.YURINA_WOLYUNGNACK, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    private val jjockbae = CardData(CardClass.SPECIAL, CardName.YURINA_JJOCKBAE, MegamiEnum.YURINA, CardType.BEHAVIOR, SubType.NONE)
    private val pobaram = CardData(CardClass.SPECIAL, CardName.YURINA_POBARAM, MegamiEnum.YURINA, CardType.ATTACK, SubType.REACTION)
    private val juruck = CardData(CardClass.SPECIAL, CardName.YURINA_JURUCK, MegamiEnum.YURINA, CardType.ATTACK, SubType.FULL_POWER)

    private fun gulSa(player: PlayerEnum, game_status: GameStatus): Boolean{
        return game_status.getPlayerLife(player) <= 3
    }
    private fun yurinaCardInit(){
        cham.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 3, 1)
        ilsom.setAttack(DistanceType.CONTINUOUS, Pair(3, 3), null, 2, 2)
        ilsom.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {buff_player, buff_game_status, madeAttack ->
                cardNumberHashmap[madeAttack.card_number] == CardName.YURINA_ILSUM && gulSa(buff_player, buff_game_status)
            }, {madeAttack ->
                madeAttack.auraPlusMinus(1)
            }))
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
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, buff_game_status, madeAttack ->
                cardNumberHashmap[madeAttack.card_number] == CardName.YURINA_JURUCK && buff_game_status.getDistance() <= 2
            }, {madeAttack ->
                madeAttack.run {
                    auraPlusMinus(1); lifePlusMinus(1)
                }
            }))
            null
        })
        giback.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) {_, player, game_status, _ ->
            game_status.addConcentration(player)
            null
        })
        giback.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.PLUS, {_, _, attack -> (attack.megami != MegamiEnum.YURINA) && (attack.card_class != CardClass.SPECIAL)},
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
            if(game_status.addPreAttackZone(player, MadeAttack(card_number, CardClass.NORMAL, DistanceType.CONTINUOUS, 3,  999, Pair(1, 4), null, MegamiEnum.YURINA)) ){
                game_status.afterMakeAttack(card_number, player, null, null, null)
            }
            null
        })
        giyenbanzo.setEnchantment(4)
        giyenbanzo.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, { buff_player, buff_game_status, _ -> gulSa(buff_player, buff_game_status)}, { madeAttack ->
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
    private val mooembuck = CardData(CardClass.NORMAL, CardName.SAINE_MOOEMBUCK, MegamiEnum.SAINE, CardType.ENCHANTMENT, SubType.FULL_POWER)
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
                if(game_status.addPreAttackZone(player, MadeAttack(card_number, CardClass.NORMAL, DistanceType.CONTINUOUS, 2,  1, Pair(4, 5), null, MegamiEnum.SAINE))){
                    game_status.afterMakeAttack(card_number, player, null, null, null)
                }
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
            if(game_status.addPreAttackZone(player, MadeAttack(card_number, CardClass.NORMAL, DistanceType.CONTINUOUS, 1,  999, Pair(0, 10), null, MegamiEnum.SAINE,
                    cannot_react_normal = false,
                    cannot_react_special = false,
                    cannot_react = true
                ))){
                game_status.afterMakeAttack(card_number, player, null, null, null)
            }
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
            if(game_status.addPreAttackZone(player, MadeAttack(card_number, CardClass.NORMAL, DistanceType.CONTINUOUS, 1,  1, Pair(3, 4), null, MegamiEnum.SAINE)) ){
                game_status.afterMakeAttack(card_number, player, null, null, null)
            }
            null
        })
        yuldonghogek.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){card_number,  player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(card_number, CardClass.NORMAL, DistanceType.CONTINUOUS, 1,  1, Pair(4, 5), null, MegamiEnum.SAINE)) ){
                game_status.afterMakeAttack(card_number, player, null, null, null)
            }
            null
        })
        yuldonghogek.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(card_number, CardClass.NORMAL, DistanceType.CONTINUOUS, 2,  2, Pair(3, 5), null, MegamiEnum.SAINE)) ) {
                game_status.afterMakeAttack(card_number, player, null, null, null)
            }
            null
        })
        hangmunggongjin.setSpecial(8)
        hangmunggongjin.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_BUFF) {card_number, player, game_status, _->
            game_status.addThisTurnCostBuff(player, CostBuff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, card -> (card.card_data.card_name == CardName.SAINE_HANGMUNGGONGJIN)}, {cost ->
                cost - game_status.getPlayerAura(player.opposite())
            }))
            null
        })
        hangmunggongjin.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            game_status.auraToDistance(player.opposite(), 2)
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
    private val fullburst = CardData(CardClass.NORMAL, CardName.HIMIKA_FULLBURST, MegamiEnum.HIMIKA, CardType.ATTACK, SubType.FULL_POWER)
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
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, madeAttack ->
                    cardNumberHashmap[madeAttack.card_number] == CardName.HIMIKA_RAPIDFIRE
                }, {madeAttack ->
                    madeAttack.auraPlusMinus(1)
                    madeAttack.lifePlusMinus(1)
                }))
            }
            null
        }))
        magnumcanon.setAttack(DistanceType.CONTINUOUS, Pair(5, 8), null, 3, 2)
        magnumcanon.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            if(game_status.lifeToDust(player, 1)){
                game_status.gameEnd(player.opposite())
            }
            null
        })
        fullburst.setAttack(DistanceType.CONTINUOUS, Pair(5, 9), null, 3, 1)
        fullburst.addtext((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CHANGE_EACH_IMMEDIATE, {_, _, madeAttack ->
                cardNumberHashmap[madeAttack.card_number] == CardName.HIMIKA_FULLBURST
            }, {madeAttack ->
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
            game_status.setShrink(player.opposite())
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
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CHANGE_EACH_IMMEDIATE, {_, _, madeAttack ->
                cardNumberHashmap[madeAttack.card_number] == CardName.HIMIKA_CRIMSONZERO
            }, {madeAttack ->
                madeAttack.setBothSideDamage()
            }))
            null
        }))
        crimsonzero.addtext((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            if(game_status.getDistance() == 0){
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CHANGE_EACH_IMMEDIATE, {_, _, madeAttack ->
                    cardNumberHashmap[madeAttack.card_number] == CardName.HIMIKA_CRIMSONZERO }, {madeAttack ->
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
            game_status.coverCard(player, player, cardNameHashmapFirst[CardName.HIMIKA_SCARLETIMAGINE]!!)
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
    private val flipfan = CardData(CardClass.NORMAL, CardName.TOKOYO_FLIPFAN, MegamiEnum.TOKOYO, CardType.BEHAVIOR, SubType.FULL_POWER)
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
                    game_status.flareToAura(player, player, 1)
                    break
                }
                else if(nowCommand == CommandEnum.SELECT_TWO){
                    game_status.auraToDistance(player, 1)
                    break
                }
            }
            null
        })
        flipfan.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _->
            while (true){
                val set = mutableSetOf<Int>()
                val list = game_status.selectCardFrom(player, player, listOf(LocationEnum.COVER_CARD, LocationEnum.DISCARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, card_number
                ) { _ -> true }?: break
                set.addAll(list)
                if (set.size <= 2){
                    if(list.isNotEmpty()){
                        for (cardNumber in list){
                            game_status.popCardFrom(player, cardNumber, LocationEnum.DISCARD, true)?.let {
                                game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, true)
                            }?: game_status.popCardFrom(player, cardNumber, LocationEnum.COVER_CARD, false)?.let {
                                game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, false)
                            }
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
            if(game_status.addPreAttackZone(player, MadeAttack(card_number, CardClass.NORMAL, DistanceType.CONTINUOUS, 999,  1, Pair(3, 6), null, MegamiEnum.TOKOYO)) ){
                game_status.afterMakeAttack(card_number, player, null, null, null)
            }
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
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CHANGE_EACH_IMMEDIATE, {_, _, madeAttack ->
                cardNumberHashmap[madeAttack.card_number] == CardName.TOKOYO_ENDLESSWIND
            }, {madeAttack ->
                madeAttack.canNotReact()
            }))
            null
        })
        endlesswind.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _ ->
            while (true){
                val list = game_status.selectCardFrom(player.opposite(), player.opposite(), listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, card_number)
                { card -> card.card_data.card_type != CardType.ATTACK && card.card_data.canDiscard}
                if(list == null){
                    game_status.showSome(player.opposite(), CommandEnum.SHOW_HAND_ALL_YOUR, -1)
                    break
                }
                else{
                    if (list.size == 1){
                        val card = game_status.popCardFrom(player.opposite(), list[0], LocationEnum.HAND, true)?: continue
                        game_status.insertCardTo(player, card, LocationEnum.DISCARD, true)
                        break
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
            game_status.setConcentration(player.opposite(), 0)
            game_status.setShrink(player.opposite())
            null
        })
    }

    private val wire = CardData(CardClass.NORMAL, CardName.OBORO_WIRE, MegamiEnum.OBORO, CardType.ATTACK, SubType.NONE)
    private val shadowcaltrop = CardData(CardClass.NORMAL, CardName.OBORO_SHADOWCALTROP, MegamiEnum.OBORO, CardType.ATTACK, SubType.NONE)
    private val zangekiranbu = CardData(CardClass.NORMAL, CardName.OBORO_ZANGEKIRANBU, MegamiEnum.OBORO, CardType.ATTACK, SubType.FULL_POWER)
    private val ninjawalk = CardData(CardClass.NORMAL, CardName.OBORO_NINJAWALK, MegamiEnum.OBORO, CardType.BEHAVIOR, SubType.NONE)
    private val induce = CardData(CardClass.NORMAL, CardName.OBORO_INDUCE, MegamiEnum.OBORO, CardType.BEHAVIOR, SubType.REACTION)
    private val clone = CardData(CardClass.NORMAL, CardName.OBORO_CLONE, MegamiEnum.OBORO, CardType.BEHAVIOR, SubType.FULL_POWER)
    private val bioactivity = CardData(CardClass.NORMAL, CardName.OBORO_BIOACTIVITY, MegamiEnum.OBORO, CardType.ENCHANTMENT, SubType.NONE)
    private val kumasuke = CardData(CardClass.SPECIAL, CardName.OBORO_KUMASUKE, MegamiEnum.OBORO, CardType.ATTACK, SubType.FULL_POWER)
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
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CHANGE_EACH_IMMEDIATE, {_, _, madeAttack ->
                cardNumberHashmap[madeAttack.card_number] == CardName.OBORO_SHADOWCALTROP
            }, {madeAttack ->
                madeAttack.canNotReact()
            }))
            null
        })
        shadowcaltrop.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _ ->
            if (game_status.logger.checkThisCardUseInCover(player, card_number)){
                game_status.coverCard(player.opposite(), player, cardNameHashmapFirst[CardName.OBORO_SHADOWCALTROP]!!)
            }
            null
        })
        zangekiranbu.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 3, 2)
        zangekiranbu.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            if (game_status.logger.checkThisTurnGetAuraDamage(player.opposite())) {
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, madeAttack ->
                    cardNumberHashmap[madeAttack.card_number] == CardName.OBORO_ZANGEKIRANBU
                }, {madeAttack ->
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
                    game_status.distanceToAura(player.opposite(), 1)
                    break
                }
                else if(nowCommand == CommandEnum.SELECT_TWO){
                    game_status.auraToFlare(player.opposite(), player.opposite(), 1)
                    break
                }
            }
            null
        })
        clone.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) {card_number, player, game_status, _ ->
            while(true){
                val selected = game_status.selectCardFrom(player, player, listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, card_number)
                {card -> card.card_data.sub_type != SubType.FULL_POWER}
                if(selected == null){
                    game_status.showSome(player, CommandEnum.SHOW_COVER_YOUR, -1)
                }
                else{
                    if(selected.size == 1){
                        val selectNumber = selected[0]
                        val card = game_status.getCardFrom(player, selectNumber, LocationEnum.COVER_CARD)?: continue
                        game_status.useCardFrom(player, card, LocationEnum.COVER_CARD, false, null, null, null)
                        if(game_status.getEndTurn(player)) break
                        val secondCard = game_status.getCardFrom(player, selectNumber, LocationEnum.DISCARD)?: break
                        game_status.useCardFrom(player, secondCard, LocationEnum.DISCARD, false, null, null, null)
                        break
                    }
                }
            }
            null
        })
        bioactivity.setEnchantment(4)
        bioactivity.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.INSTALLATION, null))
        bioactivity.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHASM, null))
        bioactivity.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.RETURN_OTHER_CARD) {card_number, player, game_status, _ ->
            while(true) {
                val selected = game_status.selectCardFrom(player, player, listOf(LocationEnum.USED_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    card_number) { _ -> true }
                if (selected != null) {
                    if (selected.size == 1 && game_status.returnSpecialCard(player, selected[0])) {
                        break
                    }
                }
            }
            null
        })
        kumasuke.setSpecial(4)
        kumasuke.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 2, 2)
        kumasuke.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            for (i in 1..game_status.getPlayer(player).cover_card.size){
                if(game_status.addPreAttackZone(player, MadeAttack(card_number, CardClass.NORMAL, DistanceType.CONTINUOUS, 2,  2, Pair(3, 4), null, MegamiEnum.OBORO))){
                    game_status.afterMakeAttack(card_number, player, null, null, null)
                }

            }
            null
        })
        tobikage.setSpecial(4)
        tobikage.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) {card_number, player, game_status, react_attack ->
            while(true){
                val selected = game_status.selectCardFrom(player, player, listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, card_number)
                {card -> card.card_data.sub_type != SubType.FULL_POWER}
                if(selected == null){
                    game_status.showSome(player, CommandEnum.SHOW_COVER_YOUR, -1)
                }
                else{
                    if(selected.size == 1){
                        val selectNumber = selected[0]
                        val card = game_status.getCardFrom(player, selectNumber, LocationEnum.COVER_CARD)?: continue
                        game_status.useCardFrom(player, card, LocationEnum.COVER_CARD, true, react_attack, null, null)
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
    private val swing = CardData(CardClass.NORMAL, CardName.YUKIHI_SWING_SLASH_STAB, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.FULL_POWER)
    private val turnUmbrella = CardData(CardClass.NORMAL, CardName.YUKIHI_TURN_UMBRELLA, MegamiEnum.YUKIHI, CardType.BEHAVIOR, SubType.NONE)
    private val backwardStep = CardData(CardClass.NORMAL, CardName.YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN, MegamiEnum.YUKIHI, CardType.BEHAVIOR, SubType.REACTION)
    private val makeConnection = CardData(CardClass.NORMAL, CardName.YUKIHI_MAKE_CONNECTION, MegamiEnum.YUKIHI, CardType.ENCHANTMENT, SubType.NONE)
    private val flutteringSnowflake = CardData(CardClass.SPECIAL, CardName.YUKIHI_FLUTTERING_SNOWFLAKE, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.NONE)
    private val swayingLamplight = CardData(CardClass.SPECIAL, CardName.YUKIHI_SWAYING_LAMPLIGHT, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.NONE)
    private val clingyMind = CardData(CardClass.SPECIAL, CardName.YUKIHI_CLINGY_MIND, MegamiEnum.YUKIHI, CardType.ENCHANTMENT, SubType.FULL_POWER)
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
        backwardStep.addTextFold(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, _, game_status, _ ->
            game_status.dustToDistance(1)
            null
        })
        backwardStep.addTextUnfold(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, _, game_status, _ ->
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

    private suspend fun setStratagemByUser(game_status: GameStatus, player: PlayerEnum, card_number: Int){
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

    private val shinra = CardData(CardClass.SPECIAL, CardName.SHINRA_SHINRA, MegamiEnum.SHINRA, CardType.BEHAVIOR, SubType.NONE)
    private val iblon = CardData(CardClass.NORMAL, CardName.SHINRA_IBLON, MegamiEnum.SHINRA, CardType.ATTACK, SubType.NONE)
    private val banlon = CardData(CardClass.NORMAL, CardName.SHINRA_BANLON, MegamiEnum.SHINRA, CardType.ATTACK, SubType.REACTION)
    private val kiben = CardData(CardClass.NORMAL, CardName.SHINRA_KIBEN, MegamiEnum.SHINRA, CardType.ATTACK, SubType.FULL_POWER)
    private val inyong = CardData(CardClass.NORMAL, CardName.SHINRA_INYONG, MegamiEnum.SHINRA, CardType.BEHAVIOR, SubType.NONE)
    private val seondong = CardData(CardClass.NORMAL, CardName.SHINRA_SEONDONG, MegamiEnum.SHINRA, CardType.BEHAVIOR, SubType.REACTION)
    private val jangdam = CardData(CardClass.NORMAL, CardName.SHINRA_JANGDAM, MegamiEnum.SHINRA, CardType.ENCHANTMENT, SubType.NONE)
    private val nonpa = CardData(CardClass.NORMAL, CardName.SHINRA_NONPA, MegamiEnum.SHINRA, CardType.ENCHANTMENT, SubType.NONE)
    private val wanjeonNonpa = CardData(CardClass.SPECIAL, CardName.SHINRA_WANJEON_NONPA, MegamiEnum.SHINRA, CardType.BEHAVIOR, SubType.NONE)
    private val dasicIhae = CardData(CardClass.SPECIAL, CardName.SHINRA_DASIG_IHAE, MegamiEnum.SHINRA, CardType.BEHAVIOR, SubType.NONE)
    private val cheonjiBanBag = CardData(CardClass.SPECIAL, CardName.SHINRA_CHEONJI_BANBAG, MegamiEnum.SHINRA, CardType.ENCHANTMENT, SubType.FULL_POWER)
    private val samraBanSho = CardData(CardClass.SPECIAL, CardName.SHINRA_SAMRA_BAN_SHO, MegamiEnum.SHINRA, CardType.ENCHANTMENT, SubType.NONE)

    private fun shinraCardInit(){
        shinra.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.END_TURN_EFFECT) {card_number, player, game_status, _ ->
            if(game_status.getPlayer(player).stratagem == null){
                setStratagemByUser(game_status, player, card_number)
            }
            null
        })
        iblon.setAttack(DistanceType.CONTINUOUS, Pair(2, 7), null, 2, 999)
        iblon.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.EFFECT_INSTEAD_DAMAGE){_, player, game_status, _ ->
            if(game_status.getPlayer(player.opposite()).normalCardDeck.size >= 2){
                var index = 0
                val cardOne = game_status.getCardFrom(player.opposite(), 0, LocationEnum.YOUR_DECK_TOP)!!
                if(cardOne.card_data.canCover){
                    game_status.popCardFrom(player.opposite(), cardOne.card_number, LocationEnum.DECK, false)?.let {
                        game_status.insertCardTo(player.opposite(), it, LocationEnum.COVER_CARD, false)
                    }
                }
                else index = 1
                val cardTwo = game_status.getCardFrom(player.opposite(), index, LocationEnum.YOUR_DECK_TOP)!!
                if(cardTwo.card_data.canCover){
                    game_status.popCardFrom(player.opposite(), cardOne.card_number, LocationEnum.DECK, false)?.let {
                        game_status.insertCardTo(player.opposite(), it, LocationEnum.COVER_CARD, false)
                    }
                }
                1
            }
            else{
                0
            }
        })
        banlon.setAttack(DistanceType.CONTINUOUS, Pair(2, 7), null, 1, 999)
        banlon.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_NO_DAMAGE){_, _, _, reactedAttack ->
            reactedAttack?.makeNoDamage()
            null
        })
        banlon.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.DRAW_CARD){_, player, game_status, _ ->
            game_status.drawCard(player.opposite(), 1)
            null
        })
        kiben.setAttack(DistanceType.CONTINUOUS, Pair(3, 8), null, 999, 1)
        kiben.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.RUN_STRATAGEM){card_number, player, game_status, _ ->
            when(game_status.getStratagem(player)){
                Stratagem.SHIN_SAN -> {
                    for(i in 1..3){
                        val card: Card = game_status.popCardFrom(player.opposite(), -1, LocationEnum.YOUR_DECK_TOP, false)?: break
                        game_status.insertCardTo(player.opposite(), card, LocationEnum.COVER_CARD, false)
                    }
                    setStratagemByUser(game_status, player, SHINRA_SHINRA_CARD_NUMBER)
                }
                Stratagem.GUE_MO -> {
                    while (true){
                        val list = game_status.selectCardFrom(player.opposite(), player, listOf(LocationEnum.DISCARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, card_number)
                        {_ -> true}?: break
                        if (list.isNotEmpty()){
                            if (list.size == 1){
                                game_status.popCardFrom(player.opposite(), list[0], LocationEnum.DISCARD, true)?.let {
                                    game_status.useCardFrom(player, it, LocationEnum.DISCARD, false, null, null, null)
                                }
                            }
                        }
                        else{
                            break
                        }
                    }
                    setStratagemByUser(game_status, player, SHINRA_SHINRA_CARD_NUMBER)
                }
                null -> {}
            }
            null
        })
        inyong.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) {card_number, player, game_status, _->
            while(true){
                val selected = game_status.selectCardFrom(player.opposite(), player, listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, card_number)
                {_ -> true} ?: break
                if(selected.size == 0) break
                if(selected.size == 1){
                    val selectNumber = selected[0]
                    val card = game_status.getCardFrom(player.opposite(), selectNumber, LocationEnum.HAND)?: continue
                    if(card.card_data.card_type != CardType.ATTACK) continue
                    while(true){
                        val nowCommand = game_status.receiveCardEffectSelect(player, card_number)
                        if(nowCommand == CommandEnum.SELECT_ONE){
                            game_status.useCardFrom(player, card, LocationEnum.HAND, false, null, null, null)
                            break
                        }
                        else if(nowCommand == CommandEnum.SELECT_TWO){
                            game_status.insertCardTo(player.opposite(), game_status.popCardFrom(player.opposite(), card.card_number, LocationEnum.HAND, true)!!,
                                LocationEnum.COVER_CARD, true)
                            break
                        }
                    }
                    if(card.card_data.sub_type == SubType.FULL_POWER) game_status.setEndTurn(player, true)
                    break
                }
            }
            null
        })
        seondong.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.RUN_STRATAGEM) {_, player, game_status, _->
            when(game_status.getStratagem(player)){
                Stratagem.SHIN_SAN -> {
                    game_status.dustToDistance(1)
                    setStratagemByUser(game_status, player, SHINRA_SHINRA_CARD_NUMBER)
                }
                Stratagem.GUE_MO -> {
                    game_status.distanceToAura(player.opposite(), 1)
                    setStratagemByUser(game_status, player, SHINRA_SHINRA_CARD_NUMBER)
                }
                null -> {}
            }
            null
        })
        jangdam.setEnchantment(2)
        jangdam.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.RUN_STRATAGEM) {card_number, player, game_status, _->
            when(game_status.getStratagem(player)){
                Stratagem.SHIN_SAN -> {
                    game_status.addConcentration(player)
                    game_status.popCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE, true)?.let{
                        game_status.insertCardTo(it.player, it, LocationEnum.YOUR_DECK_TOP, true)
                    }
                    setStratagemByUser(game_status, player, SHINRA_SHINRA_CARD_NUMBER)
                }
                Stratagem.GUE_MO -> {
                    if (game_status.getPlayer(player.opposite()).hand.size <= 1){
                        game_status.setShrink(player.opposite())
                        game_status.drawCard(player.opposite(), 3)
                        while (true){
                            val list = game_status.selectCardFrom(player.opposite(), player.opposite(), listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, card_number, 2)
                            {_ -> true}?: break
                            for (cardNumber in list){
                                game_status.popCardFrom(player.opposite(), cardNumber, LocationEnum.HAND, true)?.let {
                                    game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD, true)
                                }
                            }
                            break
                        }
                    }
                    setStratagemByUser(game_status, player, SHINRA_SHINRA_CARD_NUMBER)
                }
                null -> {}
            }
            null
        })
        nonpa.setEnchantment(4)
        nonpa.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.SEAL_CARD) {card_number, player, game_status, _ ->
            if(game_status.getPlayer(player.opposite()).discard.size != 0){
                while (true){
                    val list = game_status.selectCardFrom(player.opposite(), player, listOf(LocationEnum.DISCARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, card_number)
                    {_ -> true}?: break
                    if (list.size == 1){
                        game_status.popCardFrom(player.opposite(), list[0], LocationEnum.DISCARD, true)?.let {
                            game_status.getPlayer(player).sealInformation[it.card_number] = card_number
                            game_status.insertCardTo(player, it, LocationEnum.SEAL_ZONE, true)
                        }
                        break
                    }
                }
            }
            null
        })
        nonpa.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.SEAL_CARD) {card_number, player, game_status, _ ->
            val player1 = game_status.getPlayer(player)
            val player2 = game_status.getPlayer(player.opposite())
            for(sealCardNumber in player1.sealZone.keys){
                if(player1.sealInformation[sealCardNumber] == card_number){
                    game_status.popCardFrom(player, sealCardNumber, LocationEnum.SEAL_ZONE, true)?.let {
                        game_status.insertCardTo(it.player, it, LocationEnum.DISCARD, true)
                    }
                }
            }
            for(sealCardNumber in player2.sealZone.keys){
                if(player2.sealInformation[sealCardNumber] == card_number){
                    game_status.popCardFrom(player.opposite(), sealCardNumber, LocationEnum.SEAL_ZONE, true)?.let {
                        game_status.insertCardTo(it.player, it, LocationEnum.DISCARD, true)
                    }
                }
            }
            null
        })
        wanjeonNonpa.setSpecial(2)
        wanjeonNonpa.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.SEAL_CARD){card_number, player, game_status, _ ->
            if(game_status.getPlayer(player.opposite()).discard.size != 0){
                while (true){
                    val list = game_status.selectCardFrom(player.opposite(), player, listOf(LocationEnum.DISCARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, card_number)
                    {_ -> true}?: break
                    if (list.size == 1){
                        game_status.popCardFrom(player.opposite(), list[0], LocationEnum.DISCARD, true)?.let {
                            game_status.getPlayer(player).sealInformation[it.card_number] = card_number
                            game_status.insertCardTo(player, it, LocationEnum.SEAL_ZONE, true)
                        }
                        break
                    }
                }
            }
            null
        })
        dasicIhae.setSpecial(2)
        dasicIhae.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.RUN_STRATAGEM) {card_number, player, game_status, _->
            when(game_status.getStratagem(player)){
                Stratagem.SHIN_SAN -> {
                    while (true){
                        val list = game_status.selectCardFrom(player, player, listOf(LocationEnum.DISCARD, LocationEnum.USED_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, card_number)
                        {card -> card.card_data.card_type == CardType.ENCHANTMENT}?: break
                        if (list.size == 1){
                            val card = game_status.popCardFrom(player, list[0], LocationEnum.DISCARD, true)?:
                            game_status.popCardFrom(player, list[0], LocationEnum.USED_CARD, true)?: continue
                            if(card.card_data.card_class == CardClass.SPECIAL) card.special_card_state = SpecialCardEnum.PLAYING
                            if(!card.textUseCheck(player, game_status, null)) break
                            card.use(player, game_status, null, null, null)
                            break
                        }
                    }
                    setStratagemByUser(game_status, player, SHINRA_SHINRA_CARD_NUMBER)
                }
                Stratagem.GUE_MO -> {
                    while (true){
                        val list = game_status.selectCardFrom(player.opposite(), player, listOf(LocationEnum.ENCHANTMENT_ZONE), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, card_number)
                        {card -> card.card_data.card_type == CardType.ENCHANTMENT}?: break
                        if (list.size == 1){
                            val card = game_status.popCardFrom(player, list[0], LocationEnum.ENCHANTMENT_ZONE, true)?: continue
                            game_status.cardToDust(player.opposite(), card.nap, card)
                            game_status.enchantmentDestruction(player.opposite(), card)
                            break
                        }
                    }
                    setStratagemByUser(game_status, player, SHINRA_SHINRA_CARD_NUMBER)
                }
                null -> {}
            }
            null
        })
        cheonjiBanBag.setSpecial(2)
        cheonjiBanBag.setEnchantment(5)
        cheonjiBanBag.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CHANGE_EACH_IMMEDIATE, {_, _, _ -> true}, { madeAttack ->
                madeAttack.run {
                    val temp = aura_damage; aura_damage = life_damage; life_damage = temp
                }
            }))
            null
        })
        samraBanSho.setSpecial(6)
        samraBanSho.setEnchantment(6)
        samraBanSho.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            game_status.dustToLife(player, 2)
            null
        })
        samraBanSho.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.EFFECT_WHEN_DESTRUCTION_OTHER_CARD){_, player, game_status, _ ->
            game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false)
            null
        })
        samraBanSho.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            game_status.gameEnd(player.opposite())
            null
        })
    }

    fun centrifugal(player: PlayerEnum, game_status: GameStatus): Boolean{
        return game_status.startTurnDistance + 1 < game_status.thisTurnDistance && !game_status.logger.checkThisTurnDoAttack(player)
    }

    fun checkAllSpecialCardUsed(player: PlayerEnum, game_status: GameStatus, except: Int): Boolean{
        val nowPlayer = game_status.getPlayer(player)
        if(nowPlayer.special_card_deck.isEmpty()){
            for (card in nowPlayer.enchantment_card.values){
                if(card.card_number == except) continue
                if(card.player == player && card.special_card_state != null) return false
            }
            for (card in nowPlayer.using_card){
                if(card.card_number == except) continue
                if(card.player == player && card.special_card_state != null) return false
            }
            return true
        }
        else{
            return false
        }
    }

    private val centrifugalAttack = CardData(CardClass.NORMAL, CardName.HAGANE_CENTRIFUGAL_ATTACK, MegamiEnum.HAGANE, CardType.ATTACK, SubType.NONE)
    private val fourWindedEarthquake = CardData(CardClass.NORMAL, CardName.HAGANE_FOUR_WINDED_EARTHQUAKE, MegamiEnum.HAGANE, CardType.ATTACK, SubType.NONE)
    private val groundBreaking = CardData(CardClass.NORMAL, CardName.HAGANE_GROUND_BREAKING, MegamiEnum.HAGANE, CardType.ATTACK, SubType.FULL_POWER)
    private val hyperRecoil = CardData(CardClass.NORMAL, CardName.HAGANE_HYPER_RECOIL, MegamiEnum.HAGANE, CardType.BEHAVIOR, SubType.NONE)
    private val wonMuRuyn = CardData(CardClass.NORMAL, CardName.HAGANE_WON_MU_RUYN, MegamiEnum.HAGANE, CardType.BEHAVIOR, SubType.NONE)
    private val ringABell = CardData(CardClass.NORMAL, CardName.HAGANE_RING_A_BELL, MegamiEnum.HAGANE, CardType.BEHAVIOR, SubType.NONE)
    private val gravitationField = CardData(CardClass.NORMAL, CardName.HAGANE_GRAVITATION_FIELD, MegamiEnum.HAGANE, CardType.ENCHANTMENT, SubType.NONE)
    private val grandSkyHoleCrash = CardData(CardClass.SPECIAL, CardName.HAGANE_GRAND_SKY_HOLE_CRASH, MegamiEnum.HAGANE, CardType.ATTACK, SubType.NONE)
    private val grandBellMegalobel = CardData(CardClass.SPECIAL, CardName.HAGANE_GRAND_BELL_MEGALOBEL, MegamiEnum.HAGANE, CardType.BEHAVIOR, SubType.NONE)
    private val grandGravitationAttract = CardData(CardClass.SPECIAL, CardName.HAGANE_GRAND_GRAVITATION_ATTRACT, MegamiEnum.HAGANE, CardType.BEHAVIOR, SubType.NONE)
    private val grandMountainRespect = CardData(CardClass.SPECIAL, CardName.HAGANE_GRAND_MOUNTAIN_RESPECT, MegamiEnum.HAGANE, CardType.BEHAVIOR, SubType.NONE)

    fun haganeCardInit(){
        centrifugalAttack.setAttack(DistanceType.CONTINUOUS, Pair(2, 6), null, 5, 3)
        centrifugalAttack.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION) {_, player, game_status, _->
            if(centrifugal(player, game_status)) 1
            else 0
        })
        centrifugalAttack.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.ADD_LOG) {card_number, player, game_status, _->
            game_status.logger.insert(Log(player, LogText.USE_CENTRIFUGAL, card_number, card_number))
            null
        })
        centrifugalAttack.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CARD_TO_COVER) {_, player, game_status, _ ->
            if (player == game_status.turnPlayer) {
                game_status.setEndTurn(player, true)
                for(card in game_status.getPlayer(player).hand.values){
                    if(card.card_data.canCover){
                        game_status.popCardFrom(player, card.card_number, LocationEnum.HAND, false)?.let {
                            game_status.insertCardTo(player, it, LocationEnum.COVER_CARD, false)
                        }
                    }
                }
                for(card in game_status.getPlayer(player.opposite()).hand.values){
                    if(card.card_data.canCover){
                        game_status.popCardFrom(player.opposite(), card.card_number, LocationEnum.HAND, false)?.let {
                            game_status.insertCardTo(player.opposite(), it, LocationEnum.COVER_CARD, false)
                        }
                    }
                }
                game_status.setConcentration(player, 0)
            }
            null
        })
        fourWindedEarthquake.setAttack(DistanceType.CONTINUOUS, Pair(0, 6), null, 1, 999)
        fourWindedEarthquake.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CARD_TO_COVER) {_, player, game_status, _ ->
            if (game_status.startTurnDistance + 1 < game_status.thisTurnDistance || game_status.startTurnDistance - 1 > game_status.thisTurnDistance) {
                game_status.getPlayer(player.opposite()).hand.values.randomOrNull()?.let { card ->
                    game_status.popCardFrom(player.opposite(), card.card_number, LocationEnum.HAND, true)?.let{
                        game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD, true)
                    }
                }
            }
            null
        })
        groundBreaking.setAttack(DistanceType.CONTINUOUS, Pair(0, 3), null, 2, 999)
        groundBreaking.addtext((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CHANGE_EACH_IMMEDIATE, {_, _, madeAttack ->
                cardNumberHashmap[madeAttack.card_number] == CardName.HAGANE_GROUND_BREAKING }, {madeAttack ->
                madeAttack.canNotReact()
            }))
            null
        }))
        groundBreaking.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CHANGE_CONCENTRATION) {_, player, game_status, _ ->
            game_status.setConcentration(player.opposite(), 0)
            game_status.setShrink(player.opposite())
            null
        })
        hyperRecoil.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            if(game_status.getAdjustDistance(player) >= 5){
                game_status.distanceToFlare(player, 1)
            }
            else{
                game_status.flareToDistance(player.opposite(), 1)
            }
            null
        })
        wonMuRuyn.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION) {_, player, game_status, _->
            if(centrifugal(player, game_status)) 1
            else 0
        })
        wonMuRuyn.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.ADD_LOG) {card_number, player, game_status, _->
            game_status.logger.insert(Log(player, LogText.USE_CENTRIFUGAL, card_number, card_number))
            null
        })
        wonMuRuyn.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            if(game_status.getPlayerFlare(player.opposite()) >= 3){
                game_status.flareToAura(player.opposite(), player, 2)
            }
            null
        })
        ringABell.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION) {_, player, game_status, _->
            if(centrifugal(player, game_status)) 1
            else 0
        })
        ringABell.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.ADD_LOG) {card_number, player, game_status, _->
            game_status.logger.insert(Log(player, LogText.USE_CENTRIFUGAL, card_number, card_number))
            null
        })
        ringABell.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, card_number)
                if(nowCommand == CommandEnum.SELECT_ONE){
                    game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS, {_, _, _ ->
                    true}, {madeAttack ->
                        madeAttack.run {
                            auraPlusMinus(2); lifePlusMinus(1)
                        }
                    }))
                    break
                }
                else if(nowCommand == CommandEnum.SELECT_TWO){
                    game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.PLUS, {_, _, _ -> true},
                        { attack -> attack.plusMinusRange(1, false)
                        }))
                    game_status.addThisTurnAttackBuff(player, Buff(card_number,1, BufTag.CHANGE_EACH, { _, _, _ -> true},
                        { attack -> attack.canNotReact()
                        }))
                    break
                }
            }
            null
        })
        gravitationField.setEnchantment(2)
        gravitationField.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            if(game_status.getFullAction(player)){
                game_status.distanceToAura(player, 2)
            }
            game_status.distanceToAura(player, 1)
            null
        })
        gravitationField.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHANGE_SWELL_DISTANCE) {_, _, _, _ ->
            -1
        })
        grandSkyHoleCrash.setSpecial(4)
        grandSkyHoleCrash.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 1000, 1000)
        grandSkyHoleCrash.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CHANGE_EACH_IMMEDIATE, {_, _, _ ->
                true
            }, {madeAttack ->
                madeAttack.Chogek()
            }))
            null
        })
        grandSkyHoleCrash.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE, {_, _, _ ->
                true
            }, {madeAttack ->
                madeAttack.run {
                    val temp = abs(game_status.getAdjustDistance(player) - game_status.startTurnDistance)
                    tempForGrandSkyHole = temp;
                    aura_damage = temp;
                    life_damage = if(temp % 2 == 0) temp / 2 else temp / 2 + 1
                }
            }))
            null
        })
        grandSkyHoleCrash.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, madeAttack ->
                (madeAttack.tempForGrandSkyHole
                    ?: abs(game_status.getAdjustDistance(player) - game_status.startTurnDistance)) !=
                        abs(game_status.getAdjustDistance(player) - game_status.startTurnDistance)
            }, {madeAttack ->
                madeAttack.run {
                    val beforeAura = tempForGrandSkyHole!!
                    val beforeLife = if(tempForGrandSkyHole!! % 2 == 0) tempForGrandSkyHole!! / 2 else tempForGrandSkyHole!! / 2 + 1
                    val nowAura = abs(game_status.getAdjustDistance(player) - game_status.startTurnDistance)
                    val nowLife = if(nowAura % 2 == 0) nowAura / 2 else nowAura / 2 + 1
                    lifePlusMinus(nowLife - beforeLife); auraPlusMinus(nowAura - beforeAura)
                }
            }))
            null
        })
        grandBellMegalobel.setSpecial(2)
        grandBellMegalobel.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            if(checkAllSpecialCardUsed(player, game_status, card_number)){
                game_status.dustToLife(player, 2)
            }
            null
        })
        grandGravitationAttract.setSpecial(5)
        grandGravitationAttract.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            game_status.distanceToFlare(player, 3)
            null
        })
        grandGravitationAttract.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){card_number, player, game_status, _ ->
            if(!game_status.logger.checkThisCardUsed(player, card_number) && game_status.logger.checkUseCentrifugal(player)) 1
            else 0
        })
        grandMountainRespect.setSpecial(4)
        grandMountainRespect.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION) {_, player, game_status, _->
            if(centrifugal(player, game_status)) 1
            else 0
        })
        grandMountainRespect.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.ADD_LOG) {card_number, player, game_status, _->
            game_status.logger.insert(Log(player, LogText.USE_CENTRIFUGAL, card_number, card_number))
            null
        })
        grandMountainRespect.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) {card_number, player, game_status, _ ->
            while(true){
                val selected = game_status.selectCardFrom(player, player, listOf(LocationEnum.DISCARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, card_number)
                {card -> card.card_data.sub_type != SubType.FULL_POWER}
                if(selected == null) break
                else{
                    if(selected.size == 0) break
                    else if(selected.size <= 2){
                        val card = game_status.getCardFrom(player, selected[0], LocationEnum.DISCARD)?: continue
                        game_status.useCardFrom(player, card, LocationEnum.DISCARD, false, null, null, null)
                        if(game_status.getEndTurn(player)) break
                        if(selected.size == 2){
                            val secondCard = game_status.getCardFrom(player, selected[1], LocationEnum.DISCARD)?: break
                            game_status.useCardFrom(player, secondCard, LocationEnum.DISCARD, false, null, null, null)
                            break
                        }
                    }
                }
            }
            null
        })
    }

    private val throwKunai = CardData(CardClass.NORMAL, CardName.CHIKAGE_THROW_KUNAI, MegamiEnum.CHIKAGE, CardType.ATTACK, SubType.NONE)
    private val poisonNeedle = CardData(CardClass.NORMAL, CardName.CHIKAGE_POISON_NEEDLE, MegamiEnum.CHIKAGE, CardType.ATTACK, SubType.NONE)
    private val toZuChu = CardData(CardClass.NORMAL, CardName.CHIKAGE_TO_ZU_CHU, MegamiEnum.CHIKAGE, CardType.ATTACK, SubType.REACTION)
    private val cuttingNeck = CardData(CardClass.NORMAL, CardName.CHIKAGE_CUTTING_NECK, MegamiEnum.CHIKAGE, CardType.ATTACK, SubType.FULL_POWER)
    private val poisonSmoke = CardData(CardClass.NORMAL, CardName.CHIKAGE_POISON_SMOKE, MegamiEnum.CHIKAGE, CardType.BEHAVIOR, SubType.NONE)

    private fun makePoisonList(player: PlayerEnum, game_status: GameStatus, ): MutableList<Int>{
        val cardList = mutableListOf<Int>()
        game_status.getPlayer(player).poisonBag[CardName.POISON_PARALYTIC]?.let {
            cardList.add(it.card_number)
        }
        game_status.getPlayer(player).poisonBag[CardName.POISON_HALLUCINOGENIC]?.let {
            cardList.add(it.card_number)
        }
        game_status.getPlayer(player).poisonBag[CardName.POISON_RELAXATION]?.let {
            cardList.add(it.card_number)
        }
        return cardList
    }

    private fun chikageCardInit(){
        throwKunai.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 2, 2)
        poisonNeedle.setAttack(DistanceType.CONTINUOUS, Pair(4, 4), null, 1, 1)
        poisonNeedle.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.INSERT_POISON) {card_number, player, game_status, _ ->
            val cardList = makePoisonList(player, game_status)
            if(cardList.size != 0){
                val get = game_status.selectCardFrom(player, cardList, CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, card_number, 1)[0]
                game_status.popCardFrom(player, get, LocationEnum.POISON_BAG, false)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_TOP, false)
                }
            }
            null
        })
        toZuChu.setAttack(DistanceType.CONTINUOUS, Pair(1, 3), null, 1, 999)
        toZuChu.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.auraToDistance(player, 1)
            game_status.dustToDistance(1)
            game_status.getPlayer(player.opposite()).canNotGoForward = true
            null
        })
        cuttingNeck.setAttack(DistanceType.CONTINUOUS, Pair(0, 3), null, 2, 3)
        cuttingNeck.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.INSERT_POISON) {card_number, player, game_status, _ ->
            if(game_status.getPlayer(player.opposite()).hand.size >= 2){
                val list = game_status.selectCardFrom(player.opposite(), player.opposite(),
                    listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, card_number, 1
                ) { _ -> true }
                game_status.popCardFrom(player.opposite(), list!![0], LocationEnum.HAND, true)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD, true)
                }
            }
            null
        })
        poisonSmoke.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.INSERT_POISON) {card_number, player, game_status, _->
            val cardList = makePoisonList(player, game_status)
            if(cardList.size != 0){
                val get = game_status.selectCardFrom(player, cardList, CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, card_number, 1)[0]
                game_status.popCardFrom(player, get, LocationEnum.POISON_BAG, true)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.HAND, true)
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
        haganeCardInit()
        chikageCardInit()
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
            CardName.SHINRA_SHINRA -> return shinra
            CardName.SHINRA_IBLON -> return iblon
            CardName.SHINRA_BANLON -> return banlon
            CardName.SHINRA_KIBEN -> return kiben
            CardName.SHINRA_INYONG -> return inyong
            CardName.SHINRA_SEONDONG -> return seondong
            CardName.SHINRA_JANGDAM -> return jangdam
            CardName.SHINRA_NONPA -> return nonpa
            CardName.SHINRA_WANJEON_NONPA -> return wanjeonNonpa
            CardName.SHINRA_DASIG_IHAE -> return dasicIhae
            CardName.SHINRA_CHEONJI_BANBAG -> return cheonjiBanBag
            CardName.SHINRA_SAMRA_BAN_SHO -> return samraBanSho
            CardName.HAGANE_CENTRIFUGAL_ATTACK -> return centrifugalAttack
            CardName.HAGANE_FOUR_WINDED_EARTHQUAKE -> return fourWindedEarthquake
            CardName.HAGANE_GROUND_BREAKING -> return groundBreaking
            CardName.HAGANE_HYPER_RECOIL -> return hyperRecoil
            CardName.HAGANE_WON_MU_RUYN -> return wonMuRuyn
            CardName.HAGANE_RING_A_BELL -> return ringABell
            CardName.HAGANE_GRAVITATION_FIELD -> return gravitationField
            CardName.HAGANE_GRAND_SKY_HOLE_CRASH -> return grandSkyHoleCrash
            CardName.HAGANE_GRAND_BELL_MEGALOBEL -> return grandBellMegalobel
            CardName.HAGANE_GRAND_GRAVITATION_ATTRACT -> return grandGravitationAttract
            CardName.HAGANE_GRAND_MOUNTAIN_RESPECT -> return grandMountainRespect
            CardName.CHIKAGE_THROW_KUNAI -> return throwKunai
            CardName.CHIKAGE_POISON_NEEDLE -> return poisonNeedle
            CardName.CHIKAGE_TO_ZU_CHU -> return toZuChu
            CardName.CHIKAGE_CUTTING_NECK -> return cuttingNeck
            CardName.CHIKAGE_POISON_SMOKE -> return poisonSmoke
            CardName.CHIKAGE_TIP_TOEING -> TODO()
            CardName.CHIKAGE_MUDDLE -> TODO()
            CardName.CHIKAGE_DEADLY_POISON -> TODO()
            CardName.CHIKAGE_HAN_KI_POISON -> TODO()
            CardName.CHIKAGE_REINCARNATION_POISON -> TODO()
            CardName.CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIFE -> TODO()
            CardName.POISON_PARALYTIC -> TODO()
            CardName.POISON_HALLUCINOGENIC -> TODO()
            CardName.POISON_RELAXATION -> TODO()
            CardName.POISON_DEADLY_1 -> TODO()
            CardName.POISON_DEADLY_2 -> TODO()
            CardName.POISON_DEADLY -> TODO()
        }
    }

    fun isPoison(card_number: Int): Boolean{
        return when(card_number){
            995, 996, 997, 998, 999, 10995, 10996, 10997, 10998, 10999 -> true
            else -> false
        }
    }
}