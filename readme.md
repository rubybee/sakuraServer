# sakura arm's rule processor
해당 프로젝트는 카드 게임 후루요니의 서버 역할을 하는 프로젝트로 아래에는 해당 서버를 이용하는 클라이언트 프로그램의 소개를 간단히 기술한다.

## 1. 방 접속
![main_screen](/image_for_readme/1_connect%20room/1_main_screen.PNG)

앱을 실행하면 위와 같은 화면이 나오고 방 만들기를 클릭하면, 방을 생성할수 있고 아래와 같은 화면이 나온다

![create_room](/image_for_readme/1_connect%20room/2_create_room.PNG)  

해당  방 번호를 상대방에게 알려줘서 첫화면의 참가하기 버튼을 클릭해, 아래의 화면에서 방번호를 입력해 게임을 시작할 수 있다.

![enter_room](/image_for_readme/1_connect%20room/3_enter_room.PNG)  

## 2. 게임 시작 전 준비
그렇게 들어가면 방 만든 사람이 아래와 같은 다이얼로그를 받을 수 있고, 2명의 케릭터를 골라서 하는 쌍장요란, 혹은 3명의 케릭터를 골라서 한
명을 밴 하는 삼습일사 둘 중 한 모드를 고르게 된다.

![select_mode](/image_for_readme/2_before_game_start/1_select_mode.PNG) 

모드를 고른 뒤에는 아래와 같은 케릭터 선택 창이 나오게 되고, 2명 내지 3명의 여신을 골라 선택 완료를 누르면 그 다음 단계인 카드 선택이 나오게 된다.  

![select_character](/image_for_readme/2_before_game_start/2_select_character.PNG)  

![select_card](/image_for_readme/2_before_game_start/3_select_card.PNG)

위의 스크린 샷은 실재 카드 선택 화면으로 카드 이름에 길게 터치를 하면, 해당 카드의 실재 스크린샷이 아래와 같이 나오게 되고 이를 참고해서 일반 패 7장, 비장패 7장으 고르고 카드 선택 완료를 누르면 실재 게임 창으로 넘어간다.  

![select_card_enlargement](/image_for_readme/2_before_game_start/4_select_card_enlargement.PNG)

## 3. 실재 게임 화면  
![basic_game_screen](/image_for_readme/3_basic_game_information/1_basic_game_screen.PNG)

본 게임은, 나의 비장패 영역, 그리고 나의 손패 영역에 있는 카드를 잘 사용해 상대방의 라이프를 0으로 줄이는 게임이다.
기본 동작(집중력) 버튼을 클릭하면, 집중력을 지불해 기본 동작을 진행할수 있으며, 손패 혹은 비장패에 있는 카드를 클릭하면 아래와 같은 화면을 통해
카드를 사용할 수 있다  

![card_click](/image_for_readme/3_basic_game_information/2_card_click.PNG)  

카드 이미지 위에 카드 사용 버튼을 누르면 아래와 같은 다이얼로그가 뜨면서 카드를 사용할 수 있다.  

![card_use_select](/image_for_readme/3_basic_game_information/3_card_use.PNG)

자신의 패산, 덮음패, 버림패 영역을 클릭하면, 해당 영역에 어떤 카드가 놓여있는지 아래 사진과 같이 알 수 있고, 몇 몇 카드는 조건에 따라 손패를 사용하는 것처럼 사용하기 버튼으로 사용할 수 있다.

![card_in_other](/image_for_readme/3_basic_game_information/4_card_image_in_other.PNG)

상대와 나의 위축 여부는 아래와 같이 집중력 옆의 숫자의 색깔로 알 수 있다. (하얀색이면 위축)

![concentration](/image_for_readme/3_basic_game_information/5_cocentration.jpg)  

여신 추가 정보 버튼과 같은 경우 아래와 같이 해당 버튼을 누르면 여신 개별로 가지고 잇는 보드 정보에 대해서 알 수 있는 스크린이 나오게 구성했다

![additional_information](/image_for_readme/3_basic_game_information/6_addtional_information.PNG)  

## 4. 공격과 대응
![attack_information](/image_for_readme/4_attack/1_attack_information.PNG)  

상대 혹은 내가 공격을 수행하면 위와 같이 버프가 적용되지 않은 공격에 대한 기본 정보고 제공되고 확인을 누르면, 아래와 같이 해당 공격에 대한 
대응 여부를 결정할 수 있다.  

![react](/image_for_readme/4_attack/2_react.PNG)

손패와 비장패를 그냥 사용하듯이 사용하면 해당 공격에 대한 대응을 진행할 수 있고, 하고 싶지않다면 대응 취소 버튼을 눌러도 된다.  

## 5. 부여패

부여패를 사용하면 아래와 같이 납을 어떻게 진행할지 결정할 수 있는 다이얼로그를 받을 수 있는데, 더스트 옆에 빈공간에, 더스트에서
보낼 벚꽃결정의 숫자를 입력후 확인을 누르면, 서버에서 납을 진행하게 된다. 만약 더스트에 입력한 숫자만큼 벚꽃결정이 없을시, 재요청을 하게 된다.

![nap](/image_for_readme/5_enchantment/1_nap.PNG)  

부여패의 경우 올려진 벚꽃 결정이 0가 되면 파기를 진행하게 되는데, 개시 페이지에서 여러개의 부여패가 동시에 파기되면, 활성 플레이어(턴 플레이어)가
부여패의 파기 순서를 결정하게 된다.

![destruction](/image_for_readme/5_enchantment/2_destruction.PNG)  

위의 사진에 영역에서 나타난 부여패를 하나씩, 손패나 비장패에서 카드를 사용하듯이 선택해서 파기 시키면 된다.(사진에는 없지만, 
상대방에게도 파기 될 부여패가 있따면 해당 영역에 나타나게 된다)

## 6. 카드 효과

몇몇 카드는 여러 효과들 중에 하나의 효과를 선택하게 되는데 그 경우, 아래와 같은 다이얼로그가 나오게 된다. 가능한 선택지 중에서 하나를 고르면 된다.

![card_effect](/image_for_readme/6_card_effect/1_select_card_effect.png)

## 7. 카드를 선택하는 카드 효과  

몇몇 카드는 특정 영역에서 카드를 골라서, 어떠한 효과를 불러일으키는데, 그 경우 아래와 같은 순서로, 카드를 고르게 된다.  


### 1. 카드 효과에 대한 안내문
![card_select_dialog](/image_for_readme/7_card_effect_card_select/1_card_select_dialog.jpg)   

위와 같은 안내문이 나오게 되고, 해당 안내문을 참고해서, 카드를 선택한다  

### 2. 카드 선택
![card_select_pool](/image_for_readme/7_card_effect_card_select/2_card_select_pool.jpg)  

부여패 처럼 왼쪽 위에 선택 가능한 카드가 나오게 되고 해당 카드들을 클릭하면 아래와 같이 카드가 확대되서 나온다.

![card_enlargement](/image_for_readme/7_card_effect_card_select/3_card_enlargement_select.jpg)  

카드 선택 버튼을 누르게 되면 해당 카드는 위에서 바로 아래로 다음 이미지와 같이 이동하게 된다.  

![card_move](/image_for_readme/7_card_effect_card_select/4_card_move.jpg)  

### 3. 선택 종료  

조건에 맞춰서, 카드를 선택하고 나면 선택 종료 버튼을 눌러서 선택을 마치면 된다.  

## 8. TODO
### 1. 카드 버전
아키나와 시스이의 카드가 신막 8시즌을 기준으로 적용되어 있으나, 다른 카드들은 7-2 시즌을 기준으로 적용되어 있음, 
이를 위해 서버와 클라이언트 양 사이드로 버전에 따라 다른 카드 데이터를 적용할 수 있는 기능은 반드시 추가 되어야 할 것으로 보임















