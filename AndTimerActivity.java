package com.AT;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class AndTimerActivity extends Activity implements OnClickListener {
    
	ImageView MV1,MV2,SV1,SV2;
	Button Min_P,Min_M,Sec_P,Sec_M;
	Button MS_bt,SP_bt;
	EditText Edit_M,Edit_S;//CustomDialog
	//Xml 요소들
	boolean AndRun = false;
	boolean AlarmRun = false;
	/*
		AndRun => Thread의 작동/비작동을 구분하는 변수
		AlarmRun => 카운트가 끝났을때 Start전환/다이얼로그출력 을 시행하기위한 변수
	*/
	public int defalut = 10;				 //초기값,환경설정에서 바꿀 있을때를 가정해 public으로 설정
	int M1_Count = 0,M2_Count = 0,S1_Count = 0,S2_Count = 0; //카운트
	int M1_Dis = 0,M2_Dis = 0,S1_Dis = 0,S2_Dis = 0; 	 //다이얼로그용
	int Minute = 0,Second = 0;				 //카운트에서 값을받아 집어넣기위한 변수
	int Min_Edit = 0,Sec_Edit = 0; 				 //설정에서 +,-의 영향을 받는값
	int ClickCount = 0;					 //ALL 카운트 0일때 하위단계 실행 방지
	int sndID;						 //다이얼로그 출력할때 소리 출력용
	SoundPool sndpl;
	Thread th2 = new Thread(new TimeThread());//Thread;
	AlertDialog.Builder Al_Ti = null;
	
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);
	        
	        MV1 = (ImageView)findViewById(R.id.M1);
	        MV2 = (ImageView)findViewById(R.id.M2);
	        SV1 = (ImageView)findViewById(R.id.S1);
	        SV2 = (ImageView)findViewById(R.id.S2);
	        MS_bt = (Button)findViewById(R.id.MSButton);
	        SP_bt = (Button)findViewById(R.id.SPButton);
	        
	        sndpl = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
	        sndID = sndpl.load(this,R.raw.ringin, 1);
	        
	        MS_bt.setOnClickListener(this);
	        SP_bt.setOnClickListener(this);
	        
	        M1_Count = defalut/10;
	        M2_Count = defalut%10;
	       
	        AHandler.sendMessage(AHandler.obtainMessage());
	        //초기값 반영하고 핸들러한테 메세지
	    }
	
	Handler AHandler = new Handler(){
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			MV1.setImageResource(resource(M1_Count));
			//MV1 End ( 0 ~ 6 )
			MV2.setImageResource(resource(M2_Count));
			//MV2 End ( 0 ~ 9 )
			SV1.setImageResource(resource(S1_Count));
			//SV1 End ( 0 ~ 6 )
			SV2.setImageResource(resource(S2_Count));
			//SV2 End ( 0 ~ 9 )
			
			
			if(AlarmRun)
			{	
				SP_bt.setText("Start"); //끝나고 stop -> start
				
				M1_Count = M1_Dis;
				M2_Count = M2_Dis;
				S1_Count = S1_Dis;
				S2_Count = S2_Dis;
				//다이얼로그 보여주기전 미리 설정값 변경
				
					Al_Ti = new AlertDialog.Builder(AndTimerActivity.this);
					Al_Ti.setTitle("요리시간이 끝났습니다!"); 
					Al_Ti.setMessage("설정했던 시간으로 다시할까요?");
					
					sndpl.play(sndID, 1, 1, 1, 1000, 1);
					
					
					Al_Ti.setPositiveButton("확인", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
								
							Thread_Start();
							sndpl.autoPause();
						}

					});
		        
					
					Al_Ti.setNegativeButton("취소", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							
							sndpl.autoPause();
						}

					}).show();
					
				
				
				AlarmRun = false; //특정이벤트에만 발동하도록 다쓰면 꺼준다
			}
			
		}
		
	};//end of Handler
	
	int resource(int count){
		int res = 0;
			switch(count){
				case 0 :	res = R.drawable.zero;	break;
				case 1 :	res = R.drawable.one;	break;
				case 2 :	res = R.drawable.two;	break;
				case 3 :	res = R.drawable.three;	break;
				case 4 : 	res = R.drawable.four;	break;
				case 5 :	res = R.drawable.five;	break;
				case 6 :	res = R.drawable.six;	break;
				case 7 :	res = R.drawable.seven;	break;
				case 8 :	res = R.drawable.eight;	break;
				case 9 :	res = R.drawable.nine;	break;
			}
		return res;
	}
	//0~9의 경우에 따라 카운트가 움직일수있도록 만든 저장소(이미지 파일 필요)
	
	public void onClick(View v) {
		
		switch(v.getId()){
		case R.id.MSButton:
		//설정. 버튼을 누를경우
			
			if(SP_bt.getText().equals("Stop"))
			{
				break;
			}
			//Start 도중 -> 설정창 이동 불가능
			
			Context Tcontext = AndTimerActivity.this;
			AlertDialog.Builder bulider;
			AlertDialog dialog;
			LayoutInflater inflater = (LayoutInflater) Tcontext.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.customdialog,(ViewGroup)findViewById(R.id.customdialog_layout));
			
			Edit_M = (EditText) layout.findViewById(R.id.Edit_M1);
			Edit_S = (EditText) layout.findViewById(R.id.Edit_S1);
			Min_P = (Button) layout.findViewById(R.id.Min_P);
			Min_M = (Button) layout.findViewById(R.id.Min_M);
			Sec_P = (Button) layout.findViewById(R.id.Sec_P);
			Sec_M = (Button) layout.findViewById(R.id.Sec_M);
			//CustomDialog 계산용으로 에딧텍스트,버튼 생성
			
			Minute = M1_Count*10 + M2_Count;
			Second = S1_Count*10 + S2_Count;
			Min_Edit = Minute;
			Sec_Edit = Second;
			//편의 고려하여 설정값이 타이머시간을 따라가도록 사전에 정의
			
			Edit_M.setText("" + Minute);
			Edit_S.setText("" + Second);
			
			Min_P.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Min_Edit++;
					if(Min_Edit == 60)
					{
						Min_Edit = 0;
					}
					Edit_M.setText("" + Min_Edit);
					
				}
			});
			Min_M.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Min_Edit--;
					if(Min_Edit == -1)
					{
						Min_Edit = 59;
					}	
					Edit_M.setText("" + Min_Edit);
					
				}
			});
			Sec_P.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Sec_Edit++;
					if(Sec_Edit == 100)
					{
						Sec_Edit = 0;
					}
					Edit_S.setText("" + Sec_Edit);
				}
			});
			Sec_M.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Sec_Edit--;
					if(Sec_Edit == -1)
					{
						Sec_Edit = 99;
					}
					Edit_S.setText("" + Sec_Edit);
				}
			});
			
			bulider = new AlertDialog.Builder(Tcontext);
			bulider.setView(layout);
			dialog = bulider.create(); //bulider.create()후에 구성요소 생성을 할수있다.
			dialog.setTitle("타이머 설정");
			
			dialog.setButton("확인",new DialogInterface.OnClickListener() { //버튼 생성
				
				public void onClick(DialogInterface dialog, int which) {
					
					M1_Dis = M2_Dis = S1_Dis = S2_Dis = 0; //저장값 초기화
					
					/*
					if(Edit_M.length() != 0){
						
						Minute = Integer.parseInt(Edit_M.getText().toString());
						if((Minute <= 60))
						{
							M1_Count = Minute / 10;
							M2_Count = Minute % 10;
							
							M1_Dis = M1_Count;
							M2_Dis = M2_Count;
						}
					}
					else
					{
						M1_Count = 0;
						M2_Count = 0;
					}
					
					if(Edit_S.length() != 0){
						Second = Integer.parseInt(Edit_S.getText().toString());
						if(Second>60){
							M2_Count += Second/60;
							Second -= (Second/60)*60;
						}
					
						S1_Count = Second/10;
						S2_Count = Second % 10;
						
						S1_Dis = S1_Count;
						S2_Dis = S2_Count;
					}
					else
					{
						S1_Count = 0;
						S2_Count = 0;
					}
					*/
					Minute = Integer.parseInt(Edit_M.getText().toString());
					Second = Integer.parseInt(Edit_S.getText().toString());
					
					M1_Count = Minute / 10;
					M2_Count = Minute % 10;
					S1_Count = Second/10;
					S2_Count = Second % 10;
					
					M1_Dis = M1_Count;
					M2_Dis = M2_Count;
					S1_Dis = S1_Count;
					S2_Dis = S2_Count;
					//텍스트내 빈값없으므로 계산 한번에 가능,초기화도 불필요
					
					AHandler.sendMessage(AHandler.obtainMessage());
					dialog.dismiss();
				}		  //onClick(확인)
			});			  //DialogOnClickListener(확인)
			
			dialog.setButton2("취소",new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					
					Toast.makeText(AndTimerActivity.this,"타이머설정이 취소되었습니다",Toast.LENGTH_SHORT).show();
					dialog.dismiss();
				}		  //onClick(취소)
			});			  //DialogOnClickListenner(취소)			
			dialog.show();//show
			
			break;
			
		case R.id.SPButton:
		//Start. 버튼을 누를경우
			ClickCount = M1_Count + M2_Count + S1_Count + S2_Count;
			//all 카운트 0일때 실행을 막는다.
			if( ClickCount != 0 )
			{
				if( SP_bt.getText().equals("Start") ) 
				{
					Thread_Start();
				}
				else
				{
					Thread_Stop();
				}
				break;
			}//if all end
		}
	}
		void Thread_Start()
		{
			Thread th2 = new Thread(new TimeThread());//Thread;
			SP_bt.setText("Stop");
			AndRun = true;
			th2.start(); // Click -> th.start -> Thread -> [run() -> Handler][반복]
		}
		void Thread_Stop()
		{
			AndRun = false;
			SP_bt.setText("Start");
		}
		//시작,끝을 다른 위치에서 사용이 용이하도록 함수로 씀
		
	class TimeThread implements Runnable{
		public void run(){
			while(AndRun){
				
					S2_Count--;
					
				if( S2_Count == -1) 
				{
					S1_Count--;
					S2_Count = 9;
				}
				if( S1_Count == -1)
				{
					M2_Count--;
					S1_Count = 5;
				}
				if( M2_Count == -1)
				{
					M1_Count--;
					M2_Count = 9;
				}
//				AHandler.sendMessageDelayed(AHandler.obtainMessage(),1000);
				AHandler.sendMessage(AHandler.obtainMessage());
				result();
				try {
				Thread.sleep(1000);
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}
		}//스레드 내용
	
		Boolean result(){
			if( M1_Count == 0 && M2_Count == 0 && S1_Count == 0 && S2_Count == 0)
			{
				AndRun = false;
				AlarmRun = true;
				ClickCount = 0; // 여기서 초기화하지 않으면 카운트 0일때 멋대로 실행된다.
			}
			return AndRun;
		}//all 카운트 0일때의 결과를 체크하여 잡아내기위한 논리함수
	}
}
