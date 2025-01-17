package com.ookiisoftware.protips.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ookiisoftware.protips.R;
import com.ookiisoftware.protips.adapter.CustomViewPager;
import com.ookiisoftware.protips.adapter.SectionsPagerAdapter;
import com.ookiisoftware.protips.auxiliar.Const;
import com.ookiisoftware.protips.auxiliar.Import;
import com.ookiisoftware.protips.auxiliar.notification.MyNotificationManager;
import com.ookiisoftware.protips.auxiliar.notification.SegundoPlanoService;
import com.ookiisoftware.protips.fragment.FeedFragment;
import com.ookiisoftware.protips.fragment.NotificationsFragment;
import com.ookiisoftware.protips.fragment.PerfilFragment;
import com.ookiisoftware.protips.fragment.TipstersFragment;
import com.ookiisoftware.protips.modelo.Activites;
import com.ookiisoftware.protips.modelo.AutoComplete;
import com.ookiisoftware.protips.modelo.Token;
import com.ookiisoftware.protips.modelo.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import java.util.TimeZone;

import static com.ookiisoftware.protips.auxiliar.Const.classes.fragments.pagerPosition.FEED;
import static com.ookiisoftware.protips.auxiliar.Const.classes.fragments.pagerPosition.PERFIL;
import static com.ookiisoftware.protips.auxiliar.Const.classes.fragments.pagerPosition.TIPSTERS;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener, NavigationView.OnNavigationItemSelectedListener {

    //region Variáveis

    private static final String TAG = "MainActivity";

    private MainActivity activity;
    private String meuId;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private boolean inPrimeiroPlano;
    private int pageSelect;

    private DatabaseReference refAllTipsters;
    private ChildEventListener eventAllTipsters;
//    private boolean isTipster;

    //Elementos do Layout
    private CustomViewPager viewPager;
    private DrawerLayout drawer;
    private BottomNavigationView navView;
    // Itens do Menu
    private MenuItem menu_meus_posts;
    private MenuItem menu_pesquisa;
    //Fragments
    public FeedFragment feedFragment;
    public PerfilFragment perfilFragment;
    public TipstersFragment tipstersFragment;
    public NotificationsFragment notificationsFragment;

    // Titulo personalizado pra ActionBar
    private AppCompatTextView action_bar_titulo_1, action_bar_titulo_2;

    //endregion

    //region Overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        startService(new Intent(activity, SegundoPlanoService.class));

        Import.Alert.d(TAG, "Timezone1", TimeZone.getDefault().getID());
        Import.Alert.d(TAG, "Timezone2", TimeZone.getDefault().getDisplayName());

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        inPrimeiroPlano = true;
        refAllTipsters.addChildEventListener(eventAllTipsters);

        if (Import.getFirebase.isTipster())
            baixarEsportes();
    }

    @Override
    protected void onStop() {
        super.onStop();
        inPrimeiroPlano = false;
        refAllTipsters.removeEventListener(eventAllTipsters);
        try {
            Import.getFirebase.saveUser(activity);
        } catch (Exception e) {
            Import.Alert.e(TAG, "onStop", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu_pesquisa = menu.findItem(R.id.menu_pesquisa);
        menu_meus_posts = menu.findItem(R.id.menu_meus_postes);

        menu_meus_posts.setVisible(Import.getFirebase.isTipster());

        SearchView sv_pesquisa = (SearchView) menu_pesquisa.getActionView();
        sv_pesquisa.setImeOptions(EditorInfo.IME_ACTION_DONE);
        sv_pesquisa.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                tipstersFragment.refreshLayout.setEnabled(newText.isEmpty());
                feedFragment.refreshLayout.setEnabled(newText.isEmpty());
                tipstersFragment.getUserAdapter().getFilter().filter(newText);
                return false;
            }
        });

        swithMenu(pageSelect);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START, true);
                break;
            case R.id.menu_meus_postes: {
                item.setChecked(!item.isChecked());
                if (item.isChecked()) {
                    item.setTitle(getResources().getString(R.string.menu_todos_postes));
                    item.setIcon(getResources().getDrawable(R.drawable.ic_notification_icon_dark));
                } else {
                    item.setTitle(getResources().getString(R.string.menu_meus_postes));
                    item.setIcon(getResources().getDrawable(R.drawable.ic_notification_icon_light));
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_menu_feed:
                setPagePosition(FEED);
                break;
            case R.id.nav_menu_tipster:
                setPagePosition(TIPSTERS);
                break;
            case R.id.nav_menu_perfil:
                setPagePosition(PERFIL);
                break;
            case R.id.menu_lateral_batepapo: {
                Intent intent = new Intent(activity, BatepapoActivity.class);
                startActivity(intent);
                drawer.closeDrawer(GravityCompat.START, true);
                break;
            }
            case R.id.menu_lateral_logout: {
                Import.logOut(activity);
                break;
            }
            case R.id.menu_verificar_atualizacao:
                Import.verificar_atualizacao(activity);
                break;
        }

        return true;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {
        CharSequence title = sectionsPagerAdapter.getPageTitle(position);
        if (title != null)
            Import.organizarTituloTollbar(activity, action_bar_titulo_1, action_bar_titulo_2, title);

        swithMenu(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public void onBackPressed() {
        if(getPagePosition() != FEED)
            setPagePosition(FEED);
        else {
            if (feedFragment.scrollInTop())
                super.onBackPressed();
            else
                feedFragment.rollToTop();
        }
    }

    //endregion

    //region Métodos

    private void init() {
        //region findViewById
        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationView navigationView = findViewById(R.id.navigationView);
        viewPager = findViewById(R.id.viewPager);
        navView = findViewById(R.id.bottonNavigationView);
        drawer = findViewById(R.id.drawer);
        //endregion

        meuId = Import.getFirebase.getId();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            boolean primeiroLogin = bundle.getBoolean(Const.intent.PRIMEIRO_LOGIN);
            pageSelect = bundle.getInt(Const.intent.PAGE_SELECT);
            if (primeiroLogin) {
                Intent intent = new Intent(activity, PerfilActivity.class);
                intent.putExtra(Const.intent.PRIMEIRO_LOGIN, true);
                startActivity(intent);
            }
        }

        try {
            feedFragment = new FeedFragment(activity);
            perfilFragment = new PerfilFragment(this);
            tipstersFragment = new TipstersFragment(activity, false);
            notificationsFragment = new NotificationsFragment(activity, false);

            Import.activites = new Activites();
            Import.activites.setMainActivity(this);
        } catch (Exception e) {
            Import.Alert.e(TAG, "init", e);
            Import.Alert.toast(activity, getResources().getString(R.string.erro_init_main_activity));
            Import.irProLogin(activity);
        }

        //region Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_menu));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_action_bar);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        action_bar_titulo_1 = findViewById(R.id.action_bar_titulo_1);
        action_bar_titulo_2 = findViewById(R.id.action_bar_titulo_2);
        //endregion

        MyNotificationManager.criarChannelNotification(activity);
        getDeviceToken();

        //region NavigationView
        navigationView.setNavigationItemSelectedListener(this);
        navView.setOnNavigationItemSelectedListener(this);
        navView.inflateMenu(R.menu.nav_menu_main);
        //endregion

        //region viewPager
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),
                activity, feedFragment, tipstersFragment, perfilFragment, notificationsFragment);

        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.addOnPageChangeListener(this);
        setPagePosition(pageSelect);
        //endregion

        atualizarMeusDados();
        getAllTipsters();
    }

    private void swithMenu(int position) {
        boolean recolherSV = true;
        switch (position) {
            case FEED:
                navView.setSelectedItemId(R.id.nav_menu_feed);
                break;
            case TIPSTERS:
                navView.setSelectedItemId(R.id.nav_menu_tipster);
                recolherSV = false;
                break;
            case PERFIL:
                navView.setSelectedItemId(R.id.nav_menu_perfil);
                break;
        }
        if (menu_pesquisa != null) {
            if (recolherSV && menu_pesquisa.isActionViewExpanded()) {
                menu_pesquisa.collapseActionView();
            }
            menu_pesquisa.setVisible(position == TIPSTERS);
        }

        menu_meus_posts.setVisible(position == FEED && Import.getFirebase.isTipster());
    }

    private void baixarEsportes() {
        Import.getFirebase.getReference()
                .child(Const.firebase.child.AUTO_COMPLETE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            AutoComplete item = dataSnapshot.getValue(AutoComplete.class);
                            if (item == null) {
                                throw new Exception("item == null");
                            } else {
                                Import.get.setAutoComplete(item);
                            }
                        } catch (Exception e) {
                            Import.get.setAutoComplete(new AutoComplete());
                            Import.Alert.e(TAG, "baixarEsportes", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }

    private void getAllTipsters() {
        refAllTipsters = Import.getFirebase.getReference()
                .child(Const.firebase.child.USUARIO);

        eventAllTipsters = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    User item = dataSnapshot.getValue(User.class);
                    if (item == null)
                        return;
                    if (item.getDados().isBloqueado())
                        return;
                    if (item.getDados().getId().equals(meuId))
                        return;

                    String id = item.getDados().getId();

                    if (item.getDados().isTipster()) {
                        Import.get.tipsters.add(item);
                    }

                    if (Import.getFirebase.getTipster().getSeguidores().containsValue(id)) {
                        Import.get.seguidores.add(item);
                    }

                    if (Import.getFirebase.getTipster().getSeguindo().containsValue(id)) {
                        Import.get.seguindo.add(item);
                    }

                    tipstersFragment.adapterUpdate();
                    feedFragment.adapterUpdate();
                } catch (Exception ex) {
                    Import.Alert.e(TAG, ex);
                    Import.Alert.d(TAG, "onChildAdded", dataSnapshot.getKey());
                }
                try {
                    feedFragment.refreshLayout.setRefreshing(false);
                    tipstersFragment.refreshLayout.setRefreshing(false);
                } catch (Exception ignored) {}
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();

                Import.get.tipsters.remove(key);
                Import.get.seguindo.remove(key);
                Import.get.seguidores.remove(key);

                Import.Alert.d(TAG, "onChildRemoved", key);
                tipstersFragment.adapterUpdate();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
    }

    public void feedUpdate() {
        onStop();
        onStart();
    }

    public int getPagePosition() {
        return viewPager.getCurrentItem();
    }

    public void setPagingEnabled(boolean b) {
        viewPager.setPagingEnabled(b);
    }

    public void setPagePosition(int position) {
        viewPager.setCurrentItem(position);
    }

    public boolean isInPrimeiroPlano() {
        return inPrimeiroPlano;
    }

    private void atualizarMeusDados() {
        Import.getFirebase.getReference()
                .child(Const.firebase.child.USUARIO)
                .child(meuId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            User user = dataSnapshot.getValue(User.class);
                            if (user != null) {
                                Import.getFirebase.setUser(activity, user, true);
                                Import.get.seguindo.addAll(user.getPostes());
                            }
                        } catch (Exception e) {
                            Import.Alert.e(TAG, "atualizarMeusDados", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }

    private void getDeviceToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnSuccessListener(instanceIdResult -> {
                    User user = Import.getFirebase.getTipster();
                    Token token = new Token();
                    token.setId(instanceIdResult.getToken());
                    token.setData(Import.get.Data());

                    Import.getFirebase.setToken(token.getId());

                    user.getDados().setToken(token.getId());
                    user.salvarToken();
                    /*if (!user.getDados().getTokens().containsKey(token.getId())) {
//                        user.getDados().getTokens().put(token.getId(), token);

                        if (user.getDados().getTokens().size() > 1) {
                            //notificar aos outros tokens o novo acesso
                        }
                        Import.Alert.d(TAG, "getDeviceToken", "novo token", instanceIdResult.getToken());
                    }*/
                })
                .addOnFailureListener(e -> Import.Alert.e(TAG, "getDeviceToken", e));
    }

    //endregion
}
