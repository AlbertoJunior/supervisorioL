package com.example.biao.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.telephony.SmsManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import static android.graphics.Color.argb;
import static com.example.biao.myapplication.LeitorServer.getJSONFromAPI;
import static java.lang.System.currentTimeMillis;

import com.example.biao.myapplication.Monitoramento;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.components.LimitLine;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class telaPrincipal extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Spinner sp;
    //ProgressBar pb;
    private final Handler mHandler01 = new Handler();
    private Runnable mTimer1;
    private int tempoDeColeta = 3*1000;//tempo em milisegundos
    ArrayList<String> listaIps = new ArrayList<>();
    ArrayList<String> listaMacs = new ArrayList<>();
    boolean controleMonitoramento = true;
    Activity ac;

    ViewStub stubGrid;
    GridView gridView;
    GridViewAdapter2 gridViewAdapter;

    static List<objEsp> espList = new ArrayList<>();
    List<objEsp> preListaEsp = new ArrayList<>();

    DataPoint ponto;
    static List<objEsp> listaObj = new ArrayList<>();
    int toogle=1, toogleAntes = 1, contador=0;
    static int flag=0,x1=0;

    private LineChart mChart;
    XAxis xAxis;
    YAxis leftAxis;


    Calendar calendario1 = Calendar.getInstance();

    private double startingSample = 0;
    LineData data;
    int posicaoGeral=0;



    public static String getDate(long milliSeconds, String dateFormat)
    {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // g = (GraphView) findViewById(R.id.graphTela);
        mChart = (LineChart) findViewById(R.id.chart1);
        xAxis = mChart.getXAxis();
        leftAxis = mChart.getAxisLeft();
        calendario1.set(2017,10,3,0,0,0);
        startingSample = calendario1.getTimeInMillis();

        stubGrid = (ViewStub) findViewById(R.id.stubGrid);
        stubGrid.inflate();

        gridView = (GridView) findViewById(R.id.mygridview);
       // g.setVisibility(View.INVISIBLE);




        try {
            System.out.println("FAZENDO SCAN");
            fazerScan3();

        } catch (Exception e) {
            e.printStackTrace();
        }

        carregarMacs();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int position, long arg3) {
                //startImageGalleryActivity(position);
                System.out.println("esplist on long: "+ espList.size());
                if(espList.size()>0) {
                    System.out.println("Segura botão");
                    AlertDialog.Builder builder = new AlertDialog.Builder(arg1.getContext());
                    //define o titulo
                    builder.setTitle(espList.get(position).getApelido());
                    //define a mensagem
                    builder.setMessage("O que deseja?");
                    builder.setPositiveButton("Configurar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            //Toast.makeText(telaPrincipal.this, "Configurar=" + arg1, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(telaPrincipal.this, TelaConfiguracao.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("mac", espList.get(position).getMac());
                            bundle.putString("ip", espList.get(position).getIp());
                            bundle.putString("apelido", espList.get(position).getApelido());
                            bundle.putFloat("alerta", espList.get(position).getAlerta());
                            bundle.putFloat("sp",espList.get(position).getSp());
                            bundle.putInt("position", position);
                            bundle.putBoolean("max",espList.get(position).isLiMax());
                            bundle.putBoolean("min",espList.get(position).isLiMin());
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    });
                    //define um botão como negativo.
                    builder.setNegativeButton("Excluir", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            //Toast.makeText(telaPrincipal.this, "excluir=" + arg1, Toast.LENGTH_SHORT).show();
                            AlertDialog.Builder builder = new AlertDialog.Builder(telaPrincipal.this);
                            //define o titulo
                            builder.setTitle(espList.get(position).getApelido());
                            //define a mensagem
                            builder.setMessage("Realmente deseja excluir "+ espList.get(position).getApelido() +"?");
                            builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    listaMacs.remove(espList.get(position).getMac());
                                    salvarMacs(listaMacs);
                                    espList.remove(position);
                                    criarGrid(espList);
                                   // g.setVisibility(View.INVISIBLE);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getParent().getBaseContext(), "Excluido com sucesso", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            });
                            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            builder.create();
                            builder.show();
                        }
                    });

                    builder.create();
                    //Exibe
                    builder.show();

                }
                return false;
            }
        });


        mTimer1 = new Runnable() {
            @Override
            public void run() {
                if(flag==1){
                    criarGrid(espList);
                    System.out.println("espList: "+espList.size());
                    //flag=2;
                }
                Collections.sort(espList,objEsp.POR_STATUS);
                System.out.println("Controle monitoramento" +controleMonitoramento);
                buscarDados();
                if(flag==1){
                    data = new LineData(espList.get(posicaoGeral).getDataset());
                    mChart.setData(data);
                    //mChart.resetZoom();
                    mChart.invalidate();
                    //mChart.setVisibleYRange(Float.parseFloat(espList.get(posicaoGeral).getTemperatura()) - 30, Float.parseFloat(espList.get(posicaoGeral).getTemperatura() + 30),YAxis);
                    //mChart.fitScreen();
                }

                if(controleMonitoramento == true){
                    TelaMensagem.monitorar(espList);
                }


                mHandler01.postDelayed(this, tempoDeColeta);
            }

        };
        mHandler01.postDelayed(mTimer1, tempoDeColeta);
    }

    public long tempo(){
        long time = System.currentTimeMillis();
        // long timeSecs = (long)(time / 1000) * 1000;
        System.out.println("Tempo (ms): " + time);
        // System.out.println("Tempo (s): " + timeSecs);
        return time;
    }

    public void criarGrid(List<objEsp> lista){
        System.out.println("CRIANDO GRID: "+lista.size());
        if(lista.size()>0) {
            gridViewAdapter = new GridViewAdapter2(this, R.layout.grid_item2, lista);
            gridViewAdapter.notifyDataSetChanged();
            gridView.setAdapter(gridViewAdapter);
            // mChart.setVisibility(View.INVISIBLE);
            gridView.setOnItemClickListener(onItemClick);
            gridView.setVisibility(View.VISIBLE);
            // criarGrafico();

            definirColunasGridView();
        }
    }



    private double getDateX() {

        long totalMilliSeconds = System.currentTimeMillis();
        long totalSeconds = totalMilliSeconds / 1000;

        // 求出现在的秒
        long currentSecond = totalSeconds % 60;

        // 求出现在的分
        long totalMinutes = totalSeconds / 60;
        long currentMinute = totalMinutes % 60;

        // 求出现在的小时
        long totalHour = totalMinutes / 60;
        totalHour=totalHour+8; //中国时区的偏移
        long currentHour = totalHour % 24;

        double myTime = currentSecond + currentMinute * 60 + currentHour * 60
                * 60;
        return myTime;

    }


    private void definirColunasGridView() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        System.out.println("Valor de X: "+ width+"  Valor de y: "+height);

        if(width <= 480){
            gridView.setNumColumns(2);
        }else if(width <= 540){
            gridView.setNumColumns(3);
        }else if (width <= 720){
            gridView.setNumColumns(4);
        }else if (width <= 1300) {
            gridView.setNumColumns(4);
        }else if(width <= 1800) {
            gridView.setNumColumns(6);
        }else{
            gridView.setNumColumns(0xffffffff);
        }


    }

    protected float getRandom(float range, float startsfrom) {
        return (float) (Math.random() * range) + startsfrom;
    }

    private void criarGrafico() {
        Calendar calendar = Calendar.getInstance();
        Date d1 = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date d2 = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date d3 = calendar.getTime();

       mChart.getDescription().setEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        mChart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        mChart.setHighlightPerDragEnabled(true);


        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);
        mChart.setViewPortOffsets(0f, 0f, 0f, 0f);



        // add data
        // setData(100, 30);
        mChart.invalidate();




        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setTextSize(14f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(Color.rgb(255, 192, 56));
        xAxis.setCenterAxisLabels(true);
        // xAxis.setGranularity(1f); // one hour
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                System.out.println("Valor formatado: " + value);
                return mFormat.format(new Date((long) (value + startingSample)));
            }
        });


        leftAxis.setTypeface(Typeface.DEFAULT);
        leftAxis.setTextSize(14f);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setAxisMaxValue(100f);
        leftAxis.setAxisMinValue(-10f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(-10f);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setGranularity(1f);
        leftAxis.setLabelCount(6);
        leftAxis.setTextColor(Color.rgb(255, 192, 56));
        //leftAxis.setCenterAxisLabels();

    }


    public void buscarDados(){


        for(int i=0;i<espList.size();i++){
            // Log.i("teste: " + i, "Iniciando leitura do Objeto " + i);
            String temperatura;
            int tensao;
            try {
                String url = ("http://" + espList.get(i).getIp());
                temperatura = getJSONFromAPI(url);
                Log.i("teste url: " + i, "http://" + espList.get(i).getIp());
                // System.out.println(temperatura);
                if (temperatura.equals("")) {
                    temperatura = "0";
                    espList.get(i).setStatus(3);//se não tiver conexão
                }else{
                    String[] aux2 = new String[4];
                    aux2 = temperatura.split(";",3);
                    temperatura = aux2[0].toString();
                    // System.out.println("aux 2"+aux2);
                    // System.out.println("temperatura"+ temperatura);
                    tensao = Integer.parseInt(aux2[1]);
                    espList.get(i).setStatus(0);
                    espList.get(i).setTensao(tensao);
                    if(tensao == 0){
                        espList.get(i).setStatus(4);// se não tiver tensão
                    }
                    if(Float.parseFloat(temperatura) > (espList.get(i).getAlerta())){
                        espList.get(i).setStatus(2);// temperatura elevada
                    }
                }
                // System.out.println("valor temperatura: " + temperatura);
                float temp = (Float) Float.parseFloat(temperatura);
                espList.get(i).setTemperatura(temperatura);
                // System.out.println("temp esp"+ espList.get(i).getTemperatura());
                x1++;

                Calendar calendar = Calendar.getInstance();
                // calendar.add(Calendar.MINUTE,1);
                Date da = calendar.getTime();
                long sampleTime = tempo();
                if (startingSample == 0.0) {
                    startingSample = sampleTime;
                }

                System.out.println("Sample time: " + sampleTime);
                ponto = new DataPoint((double) sampleTime, temp);
                Random gerador = new Random();
                // System.out.println("Time: " + da.getTime());
                int samples = 20;
                // espList.get(i).getSeries().appendData(ponto, true, samples);
                Entry entry = new Entry((float) (sampleTime - startingSample), temp + gerador.nextInt(10));
                espList.get(i).getDataset().addEntry(entry);

                if (espList.get(i).getDataset().getEntryCount() > samples) {
                    Entry first = espList.get(i).getDataset().getEntryForIndex(0);
                    espList.get(i).getDataset().removeEntry(first);
                }


                System.out.println(espList.get(i).getDataset().getValues());
                mChart.setData(data);
                mChart.setVisibleYRange(-50,50,AxisDependency.LEFT);
                mChart.invalidate();


                //// g.getViewport().setMinX(sampleTime - samples * 0.5 * tempoDeColeta);
                //// g.getViewport().setMaxX(sampleTime);

                //// g.onDataChanged(false, false);

                // System.out.println(espList.get(i).toString());
                //DataPoint pontoAlarme;
                //pontoAlarme = new DataPoint(x1,espList.get(i).getAlerta());
                //espList.get(i).getSeriesAlarme().appendData(pontoAlarme,true,20);
                // System.out.println("Preparando pra gravar");
                salvarArquivo((float) (sampleTime - startingSample), temp + gerador.nextInt(10), espList.get(i).getMac());
            }catch (Exception e){

            }
        }

    }



    AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //Do any thing when user click to item
            //Toast.makeText(getApplicationContext(), espList.get(position).getApelido(), Toast.LENGTH_SHORT).show();
            //toogle = position;

            if(espList.size()>0) {
                if(toogleAntes==1){
                    mChart.setVisibility(View.VISIBLE);
                    toogleAntes=2;
                }else{
                    mChart.setVisibility(View.INVISIBLE);
                    toogleAntes=1;
                }
                posicaoGeral = position;




               // g.getViewport().setMinY((int) (Double.parseDouble(espList.get(position).getTemperatura()) -15)   );
               // g.getViewport().setMaxY((int) (Double.parseDouble(espList.get(position).getTemperatura()) +15)   );
                if (espList.get(position).getStatus() == 2) {
                    Toast.makeText(getApplicationContext(), espList.get(position).getApelido() + " está com temperatura elevada", Toast.LENGTH_SHORT).show();
                } else if (espList.get(position).getStatus() == 3) {
                    Toast.makeText(getApplicationContext(), espList.get(position).getApelido() + " está sem conexão", Toast.LENGTH_SHORT).show();
                } else if (espList.get(position).getStatus() == 4) {
                    Toast.makeText(getApplicationContext(), espList.get(position).getApelido() + " está sem tensão", Toast.LENGTH_SHORT).show();
                }

                if (espList.size() > 0) {
                    // mChart.setVisibility(View.VISIBLE);
                    //g.setVisibility(View.VISIBLE);
                   // g.setTitle(espList.get(position).getApelido());
                   // g.removeAllSeries();
                   // g.addSeries(espList.get(position).getSeries());
                   // g.addSeries(espList.get(position).getSeriesAlarme());
                    // setData(100, 30);
                }

            }

        }
    };


    @Override
    public void onRestart() {
        super.onRestart();
        criarGrid(espList);
        buscarDados();
    }


    @Override
    public void onBackPressed() {
        /*
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        */
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tela_principal, menu);
        return true;
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_rede) {
            new Thread(new Runnable() {
                public void run() {
                    fazerScan3();
                }
            }).start();
            // Handle the camera action
        } else if (id == R.id.nav_mensagem) {
            Intent intent = new Intent(telaPrincipal.this, TelaMensagem.class);
            startActivity(intent);

        } else if (id == R.id.nav_historico) {
            Intent intent = new Intent(telaPrincipal.this, TelaHistorico.class);
            startActivity(intent);

        } else if(id == R.id.nav_ativar){
            if(item.getTitle().equals("Desativar monitoramento")){
                item.setTitle("Ativar monitoramento");
                controleMonitoramento = false;
                //item.setIcon(Drawable.createFromPath(R.drawable.));
            }else if(item.getTitle().equals("Ativar monitoramento")){
                item.setTitle("Desativar monitoramento");
                controleMonitoramento = true;
            }
        }else if(id==R.id.nav_item){
            Intent intent = new Intent(telaPrincipal.this, telaOpcao.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    private void limparArquivo(String t){
        //criar pasta
        File folder = new File(Environment.getExternalStorageDirectory() + "/Controle_esp");
        if(!folder.exists()){
            folder.mkdir();
        }
        //salvar arquivo dentro da pasta
        String nomeArquivo = t +".txt";
        File arquivo = new File(Environment.getExternalStorageDirectory(),"/Controle_esp/" + nomeArquivo);
        System.out.println("/Controle_esp/" + nomeArquivo);
        try{
            FileOutputStream salvar = new FileOutputStream(arquivo);
            String conteudo = "";
            salvar.write(conteudo.getBytes());
            salvar.close();
            //Toast.makeText(this, "",Toast.LENGTH_SHORT).show();
        }catch (FileNotFoundException e){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Arquivo não encontrado",Toast.LENGTH_SHORT).show();
                }
            });

        }catch (IOException ioe){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Erro ao limpar arquivo",Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    public void salvarIps(String ip){
        try {
            byte[] dados;

            File arq = new File(Environment.getExternalStorageDirectory(), "/Controle_esp/ips.txt");
            if(!arq.exists()) {
                arq.createNewFile();
            }
            FileOutputStream fos;
            dados = (ip +"\n").getBytes();
            fos = new FileOutputStream(arq, true);
            fos.write(dados);
            fos.flush();
            fos.close();
            // System.out.println("Funcionei !!!!!");
            // Log.i("teste: ", "Funcionei !!!!!");
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(telaPrincipal.this, "Erro ao salvar IP",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void salvarMacs(ArrayList lista){
        try {
            byte[] dados;
            limparArquivo("macs");
            File arq = new File(Environment.getExternalStorageDirectory(), "/Controle_esp/macs.txt");

            for(int a=0;a<lista.size();a++) {
                FileOutputStream fos;
                dados = (lista.get(a) + "\n").getBytes();
                fos = new FileOutputStream(arq, true);
                fos.write(dados);
                fos.flush();
                fos.close();
            }

            // System.out.println("Funcionei!!!!!");
            // Log.i("teste: ", "Funcionei!!!!!");
        }catch (Exception e){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getParent().getBaseContext(), "Erro ao salvar MAC",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void lerIps(){
        try {
            System.out.println("LENDO ARQUIVO DE IPS");
            String nome = "ips";
            String lstrlinha;
            File arq = new File(Environment.getExternalStorageDirectory(),"/Controle_esp/"+nome+".txt");
            BufferedReader br = new BufferedReader(new FileReader(arq));
            listaIps.clear();
            while ((lstrlinha = br.readLine()) != null)
            {
                listaIps.add(lstrlinha);
                //System.out.println("Lendo lista"+listaIps.get(i));
                //Log.i("teste: ","Lendo lista"+ listaIps.get(i));
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void criarObjetos(){

        //listaObj.clear();
        List<objEsp> listaTemporariaObj = new ArrayList<>();
        listaTemporariaObj.clear();
        System.out.println("Quantidade de IPS: "+ listaIps.size());
        for(int i=0;i<listaIps.size();i++){

            objEsp temp = lerObjeto(listaIps.get(i));

            if(comparador(temp)){
                salvarDadosEsp(buscador(temp));
                listaTemporariaObj.add(buscador(temp));
                //listaObj.add(temp);
            }else{
                salvarDadosEsp(temp);
                listaTemporariaObj.add(temp);
            }
            //System.out.println("JOGANDO NA LISTA de objetos:  "+temp.getMac());
            //Log.i("JOGANDO NA LISTA:   ", "JOGANDO NA LISTA de objetos:");



        }
        for(int a=0;a<listaTemporariaObj.size();a++){
            //listaTemporariaObj.get(a).setStatus(0);
        }
            for(int a=0;a<preListaEsp.size();a++){
                //preListaEsp.get(a).setIp("192.100.100.100");
            }
            //limparArquivo("macs");
            // System.out.println("Antes: "+espList.size());
            espList.clear();
            // System.out.println("Durante: "+espList.size());
            espList = listaTemporariaObj;
            // System.out.println("depois: "+espList.size());
            if(espList.size()==0){
                espList = preListaEsp;
            }

            for(int a1=0;a1<espList.size();a1++){
                // System.out.println(a1+" EspList: "+espList.get(a1).getMac());
                for(int b1=0;b1<preListaEsp.size();b1++){
                    System.out.println(b1+" preList: "+preListaEsp.get(b1).getMac());
                    if(espList.get(a1).getMac().equals(preListaEsp.get(b1).getMac())){
                        System.out.println("Igual, não adicionar");
                    }else{
                        preListaEsp.get(b1).setIp("192.168.100.100");
                        espList.add(preListaEsp.get(b1));
                        // System.out.println("Diferente, adicionar");
                    }
                }
            }
            // System.out.println("Bem depois: "+espList.size());
            // System.out.println("Prelista: "+preListaEsp.size());


            for (int k = 0; k < espList.size(); k++) {
                int cont=0;
                for(int a=0;a<listaMacs.size();a++){
                    if(espList.get(k).getMac().equals(listaMacs.get(a))){
                        cont++;
                    }
                }
                if(cont==0){
                    listaMacs.add(espList.get(k).getMac());
                }
            }

            salvarMacs(listaMacs);
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "Fim do escaneamento", Toast.LENGTH_SHORT).show();
                if(espList.size()==1){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), espList.size()+" dispositivo", Toast.LENGTH_SHORT).show();
                        }
                    });

                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), espList.size() + " dispositivos", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });


    }

    public objEsp lerObjeto(String ip){
        objEsp temp = new objEsp();
        String retorno = getJSONFromAPI("http://"+ip);
        System.out.println(retorno);
        String[] sp = retorno.split(";",3);



        temp.setTemperatura(sp[0]);
        temp.setTensao(Integer.parseInt(sp[1]));
        temp.setMac(sp[2]);
        temp.setIp(ip);
        temp.setSp(-70);


        return temp;
    }

    public boolean comparador(objEsp ob){
        boolean a = false;
        File arq = new File(Environment.getExternalStorageDirectory(), "/Controle_esp/"+ ob.getMac() +".txt");
        if(arq.exists()){
            a=true;
            System.out.println("ACHEI IGUAL");
        }


        return a;
    }

    public objEsp buscador(objEsp ob){
        objEsp ativo = new objEsp();
        String lstrlinha;
        try {
            File arq = new File(Environment.getExternalStorageDirectory(), "/Controle_esp/" + ob.getMac() + ".txt");
            BufferedReader br = new BufferedReader(new FileReader(arq));
            lstrlinha = br.readLine(); //MAC
            lstrlinha = br.readLine();  //IP
            lstrlinha = br.readLine();   //Apelido
            ativo.setApelido(lstrlinha);
            lstrlinha = br.readLine();  //Alerta
            ativo.setAlerta(Float.parseFloat(lstrlinha));
            lstrlinha = br.readLine(); //SP
            ativo.setSp(Float.parseFloat(lstrlinha));
            lstrlinha = br.readLine(); //se existe limite maximo
            ativo.setLiMax(Boolean.parseBoolean(lstrlinha));
            lstrlinha = br.readLine();  //se existe limite minimo
            ativo.setLiMin(Boolean.parseBoolean(lstrlinha));

            if(ativo.isLiMax()==true){
                ativo.setLimiteMax(ativo.getSp() + ativo.getAlerta());
            }
            if(ativo.isLiMin()==true){
                ativo.setLimiteMin(ativo.getSp() - ativo.getAlerta());
            }

            ativo.setMac(ob.getMac());
            ativo.setIp(ob.getIp());



            System.out.println("Mac: "+ativo.getMac());
            System.out.println("Ip: "+ativo.getIp());
            System.out.println("Alerta: "+ativo.getAlerta());
            System.out.println("Apelido: "+ativo.getApelido());
        }catch (IOException e){
            e.printStackTrace();
        }
        return ativo;
    }

    public void salvarDadosEsp(objEsp oe){
        try {
            byte[] dados;
            File arq = new File(Environment.getExternalStorageDirectory(), "/Controle_esp/"+oe.getMac() +".txt");
            FileOutputStream fos;
            limparArquivo(oe.getMac());
            System.out.println("Dados oe"+oe.getSp());
            dados = (oe.getMac() +"\n" + oe.getIp()+"\n" + oe.getApelido() +"\n" + oe.getAlerta() +"\n"+oe.getSp()+"\n"+oe.isLiMax()+"\n"+oe.isLiMin()+"\n").getBytes();
            fos = new FileOutputStream(arq,true);
            fos.write(dados);
            fos.flush();
            fos.close();
            //System.out.println("GRAVEI!!!!!");
            //Log.i("teste: ", "GRAVEI!!!!!");
        }catch (Exception e){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getParent().getBaseContext(), "Erro ao salvar medicao",Toast.LENGTH_SHORT).show();
                }
            });

        }
    }







    public void fazerScan3(){
        new Thread(new Runnable() {
            public void run(){
                limparArquivo("ips");
                runOnUiThread(new Runnable() {
                    public void run() {

                        Toast.makeText(getApplicationContext(), "Escaneando a rede", Toast.LENGTH_SHORT).show();
                        for(int a=0;a<espList.size();a++){
                            espList.get(a).setStatus(1);
                        }
                        criarGrid(espList);
                        criarGrafico();
                    }
                });
                flag=0;

                byte[] ipBytes;
                WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                String ip2 = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                String ip3 = ip2.replace(".",":");
                System.out.println("ip2: "+ip2+ "   ip3: "+ ip3);
                InetAddress ip = null;
                try {
                    ip = InetAddress.getByName(ip2);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                ipBytes = ip.getAddress();
                String[] temp = ip3.split(":",4);

                for (int i = 96; i <= 115; i++) {
                    ipBytes[3] = (byte) i;
                    try {
                        InetAddress address = InetAddress.getByAddress(ipBytes);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    String tentativa = getJSONFromAPI("http://"+temp[0]+"."+temp[1]+"."+temp[2]+"."+i);
                    System.out.println(temp[0]+"."+temp[1]+"."+temp[2]+"."+i + "\n");
                    if (!tentativa.equals("")) {
                        salvarIps(temp[0]+"."+temp[1]+"."+temp[2]+"."+i);
                        System.out.println(temp[0]+"."+temp[1]+"."+temp[2]+"."+i);
                    }

                }

                lerIps();
                criarObjetos();
                //gridViewAdapter = new GridViewAdapter(this, R.layout.grid_item, espList);
                //gridView.setAdapter(gridViewAdapter);
                //g.setVisibility(View.INVISIBLE);
                //gridView.setOnItemClickListener(onItemClick);
                //gridView.setVisibility(View.VISIBLE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        criarGrid(espList);
                    }
                });


                flag=1;

            }
        }).start();
    }


    public void carregarMacs(){
        try {
            listaMacs.clear();
            System.out.println("LENDO ARQUIVO DE Macs");
            String nome = "macs";
            String lstrlinha;
            File arq = new File(Environment.getExternalStorageDirectory(),"/Controle_esp/"+nome+".txt");
            BufferedReader br = new BufferedReader(new FileReader(arq));
            while ((lstrlinha = br.readLine()) != null){
                listaMacs.add(lstrlinha);
                //System.out.println("Lendo lista"+listaIps.get(i));
                //Log.i("teste: ","Lendo lista"+ listaIps.get(i));
            }
            for(int i=0;i<listaMacs.size();i++){
                preListaEsp.add(buscador(new objEsp(listaMacs.get(i))));

                preListaEsp.get(i).setIp("");
            }
            //objEsp aux = new objEsp();
            //aux.setMac("18:FE:34:D8:28:BE");
            //aux.setApelido("Vacina");
            //aux.setSp(70);
            //preListaEsp.add(aux);

            criarGrid(preListaEsp);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void salvarArquivo(double time, double temperatura, String nome){
        //criar pasta
        Calendar c =  Calendar.getInstance();
        File folder = new File(Environment.getExternalStorageDirectory() + "/Controle_esp/Dados");
        if(!folder.exists()){
            folder.mkdir();
        }

        //salvar arquivo dentro da pasta
        String nomeArquivo = nome + "_"+ c.get(Calendar.YEAR)+"_"+(c.get(Calendar.MONTH)+1)+"_"+c.get(Calendar.DAY_OF_MONTH)+".txt";
        File arquivo = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"/Controle_esp/Dados/" + nomeArquivo);
        //System.out.println(Environment.getExternalStorageDirectory().getAbsolutePath());
        try{
            FileOutputStream salvar = new FileOutputStream(arquivo,true);
            String conteudo = temperatura + ";" + time +"\n"; //c.get(Calendar.HOUR_OF_DAY)+ ";" + c.get(Calendar.MINUTE)
            salvar.write(conteudo.getBytes());
            salvar.close();
            System.out.println("Gravando");
            //Toast.makeText(this, "Gravando",Toast.LENGTH_SHORT).show();
        }catch (FileNotFoundException e){
            //Toast.makeText(this, "Arquivo não encontrado",Toast.LENGTH_SHORT).show();
        }catch (IOException ioe){
            //Toast.makeText(this, "Erro",Toast.LENGTH_SHORT).show();
        }

    }





}
