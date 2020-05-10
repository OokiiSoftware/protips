package com.ookiisoftware.protips.auxiliar;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ookiisoftware.protips.R;
import com.ookiisoftware.protips.activity.MainActivity;
import com.ookiisoftware.protips.modelo.Post;
//import com.ookiisoftware.protips.modelo.Punter;
import com.ookiisoftware.protips.modelo.User;
import com.ookiisoftware.protips.modelo.Usuario;

import java.util.List;

public class SegundoPlanoService extends Service {

    private static final String TAG = "SegundoPlanoService";
    private Context getContext() {
        return this;
    }

    //region Overrides

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (Import.getFirebase.isTipster()) {
                CommandTipsterPunter();
                CommandTipsterPunterPendente();
            } else {
                CommandPunterPendenteAceito();
            }
            CommandPostes();
        } catch (Exception e) {
            Import.Alert.erro(TAG, e);
        }

        // START_STICKY serve para executar seu serviço até que você pare ele, é reiniciado automaticamente sempre que termina
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Import.Alert.msg(TAG, "onDestroy", "----");
    }

    //endregion

    //region Command

    // Aguarda novos Postes
    private void CommandPostes() {
        for (String id : Import.getFirebase.getTipster().getSeguindo().values()) {
            postAddChildEventList(id);
        }
    }

    // Quando o punter solicita para ser punter de um tipster
    private void CommandTipsterPunterPendente() {
        final String meu_id = Import.getFirebase.getId();
        final DatabaseReference ref = Import.getFirebase.getReference()
                .child(Constantes.firebase.child.USUARIO)
                .child(meu_id)
                .child(Constantes.firebase.child.SEGUIDORES_PENDENTES);

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String key = dataSnapshot.getValue(String.class);
                if (key == null)
                    return;

                Import.Alert.msg(TAG, "CommandTipsterPunterPendente", "onChildAdded", key);
                Import.getFirebase.getTipster().getSeguidoresPendentes().put(key, key);

                getSeguidorPendente(key);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getValue(String.class);
                if (key == null)
                    return;
                Import.Alert.msg(TAG, "CommandTipsterPunterPendente", "onChildRemoved", key);
                Import.getFirebase.getTipster().getSeguidoresPendentes().remove(key);

                Import.get.solicitacao.remove(key);
                try {
                    Import.activites.getMainActivity().notificationsFragment.adapterUpdate();
                    Import.activites.getMainActivity().perfilFragment.updateNotificacao();
                } catch (Exception ignored) {}
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    // Quando um Punter é adicionado na lista de 'punters' do Tipster
    private void CommandTipsterPunter() {
        final String meu_id = Import.getFirebase.getId();
        final DatabaseReference ref = Import.getFirebase.getReference()
                .child(Constantes.firebase.child.USUARIO)
//                .child(Constantes.firebase.child.TIPSTERS)
                .child(meu_id)
                .child(Constantes.firebase.child.SEGUIDORES);

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final String key = dataSnapshot.getValue(String.class);
                if (key == null)
                    return;

                DatabaseReference ref =  Import.getFirebase.getReference()
                        .child(Constantes.firebase.child.USUARIO)
//                        .child(Constantes.firebase.child.PUNTERS)
                        .child(key);

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        Punter item = dataSnapshot.getValue(Punter.class);
//                        if (item != null) {
//                            if (item.getDados().isBloqueado())
//                                return;
//
//                            Import.getFirebase.getTipster().getSeguidoresPendentes().remove(key);
//                            Punter item2 = Import.get.tipsters.findPuntersPendentes(item.getDados().getId());
//                            if (item2 != null) {
//                                Import.get.tipsters.getPuntersPendentes().remove(item2);
//                            }
//
//                            item.addTipster(meu_id);
//                            item2 = Import.get.punter.find(item.getDados().getId());
//                            if (item2 == null) {
//                                Import.get.punter.getAll().add(item);
//                                Import.get.punter.getAllAux().add(item);
//                            } else {
//                                Import.get.punter.getAll().set(Import.get.punter.getAll().indexOf(item2), item);
//                                Import.get.punter.getAllAux().set(Import.get.punter.getAllAux().indexOf(item2), item);
//                            }
//
//                            try {
//                                Import.activites.getMainActivity().notificationsFragment.adapterUpdate();
//                                Import.activites.getMainActivity().tipstersFragment.adapterUpdate();
//                                Import.activites.getMainActivity().perfilFragment.updateNotificacao();
//                            } catch (Exception ignored) {}
//                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                String key = dataSnapshot.getValue(String.class);
//                if (key == null)
//                    return;
//
//                Punter item = Import.get.punter.find(key);
//                if (item != null) {
//                    Import.get.punter.getAll().remove(item);
//                    Import.get.punter.getAllAux().remove(item);
//
//                    item.removerTipster(meu_id);
//                }
//
//                try {
//                    Import.activites.getMainActivity().tipstersFragment.adapterUpdate();
//                } catch (Exception ignored) {}
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    // Quando o tipster aceita a solicitação do punter
    private void CommandPunterPendenteAceito() {
        final String meu_id = Import.getFirebase.getId();
        final DatabaseReference ref = Import.getFirebase.getReference()
                .child(Constantes.firebase.child.USUARIO)
                .child(meu_id)
                .child(Constantes.firebase.child.SEGUINDO);

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final String key = dataSnapshot.getValue(String.class);
                if (key == null)
                    return;

                getTipster(key, false);
                postAddChildEventList(key);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getValue(String.class);
                if (key == null)
                    return;

                Import.get.seguindo.remove(key);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    /*private void CommandConversas(){
        final String usuarioLogadoId = Import.getFirebase.getId();
        final DatabaseReference fbRefConversas = Import.getFirebase.getReference()
                .child(Constantes.firebase.child.USUARIO)
                .child(usuarioLogadoId)
                .child(Constantes.firebase.child.CONVERSAS);

        ValueEventListener valueEventListenerConversas = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final StringBuilder textoNotificacao = new StringBuilder();
                final StringBuilder tituloNotificacao = new StringBuilder();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (data.getKey() != null){
                        //Na hierarquia = root > usuarios > id_usuario_logado > conversas > id_conversa
                        final DatabaseReference refTemp = fbRefConversas.child(data.getKey());

                        refTemp.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot data2 : dataSnapshot.getChildren()) {
                                    Mensagem mensagem = data2.getValue(Mensagem.class);

                                    if (mensagem != null) {
                                        String nome;
                                        {
                                            String id_conversa = Criptografia.descriptografar(mensagem.getId_conversa());
                                            // Procurar o nome do remetente nos contatos, se não tiver coloco o TipName
//                                            SQLiteContato dbContato = new SQLiteContato(getApplicationContext());
                                            Contato contato = dbContato.get(id_conversa);
                                            if (contato == null)
                                                nome = id_conversa;
                                            else
                                                nome = contato.getNome();
                                        }

                                        if (Import.SalvarMensagemNoDispositivo(getApplicationContext(), mensagem)) {
                                            Conversa conversa = new Conversa();
                                            conversa.setId(mensagem.getId_conversa());
                                            conversa.setNome_contato(nome);
                                            conversa.setUltima_msg(mensagem.getMensagem());
                                            conversa.setData(mensagem.getData_de_envio());

                                            {
                                                boolean mostrarNotificacao = false;
                                                boolean marcarMsgComoLido = false;

                                                if (mensagem.getId_remetente().equals(usuarioLogadoId)) {
                                                    marcarMsgComoLido = true;// se eu que enviei então marca como lida no meu dispositivo
                                                } else if (Import.getUsuario.getUsuarioConversa() == null) {
                                                    mostrarNotificacao = true;
                                                } else if (Import.getUsuario.getUsuarioConversa().getId().equals(conversa.getId()))
                                                    marcarMsgComoLido = true;// se eu estou conversando com a pessoa então marca como lida no meu dispositivo
//
                                                if (marcarMsgComoLido)
                                                    conversa.setLido(Constantes.CONVERSA_MENSAGEM_LIDA);
                                                else
                                                    conversa.setLido(Constantes.CONVERSA_MENSAGEM_ENVIADA);

                                                if (mostrarNotificacao) {

                                                    String msg = Criptografia.descriptografar(mensagem.getMensagem());
                                                    tituloNotificacao.append(nome).append(",");
                                                    textoNotificacao.append(nome).append(": ").append(msg).append("\n");
                                                    CriarNotificacaoDaMensagem(mensagem.getId_conversa(), tituloNotificacao.toString(), textoNotificacao.toString());
                                                }

                                            }// Verificar quem mandou a mensagem e se a conversa está aberta com o remetente

                                            Import.SalvarConversaNoDispositivo(getApplicationContext(), conversa);
                                            Import.RemoverMensagemDoFirebase(refTemp, mensagem);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                        });
                        refTemp.removeEventListener(this);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        fbRefConversas.addValueEventListener(valueEventListenerConversas);
    }*/

    //endregion

    //region notifications

    private void notificationPunterPendente(@NonNull Usuario usuario) {
        try {
            switch (Import.activites.getMainActivity().getPagePosition()) {
                case Constantes.classes.fragments.pagerPosition.NOTIFICATIONS:
                    Import.activites.getMainActivity().notificationsFragment.adapterUpdate();
                    break;
                case Constantes.classes.fragments.pagerPosition.PERFIL:
                    Import.activites.getMainActivity().perfilFragment.updateNotificacao();
                    break;
                default: {
                    String titulo = getResources().getString(R.string.app_name);
                    String texto = getResources().getString(R.string.nova_solicitação_punter) + "\n" + usuario.getNome();

                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.putExtra(Constantes.intent.PAGE_SELECT, Constantes.classes.fragments.pagerPosition.NOTIFICATIONS);
                    int channelId = Constantes.notification.id.NOVO_PUNTER_PENDENTE;
                    Import.notificacao(getContext(), intent, channelId, titulo, texto);
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    private void notificationPunterAceito(@NonNull Usuario usuario) {
        try {
            switch (Import.activites.getMainActivity().getPagePosition()) {
                case Constantes.classes.fragments.pagerPosition.FEED:
                    break;
                case Constantes.classes.fragments.pagerPosition.PERFIL:
                    Import.activites.getMainActivity().perfilFragment.updateNotificacao();
                    break;
                default: {
                    String titulo = getResources().getString(R.string.app_name);
                    String texto = getResources().getString(R.string.punter_aceito) + "\n" + getResources().getString(R.string.tipster) + ": " + usuario.getNome();

                    Intent intent = new Intent(getContext(), MainActivity.class);
                    int channelId = Constantes.notification.id.NOVO_PUNTER_ACEITO;
                    Import.notificacao(getContext(), intent, channelId, titulo, texto);
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    private void notificationNewPost(@NonNull User usuario, @NonNull Post post) {
        try {
            if (Import.activites.getMainActivity().getPagePosition() == Constantes.classes.fragments.pagerPosition.FEED &&
//                    verifyApplicationRunning(getContext()) &&
                    Import.activites.getMainActivity().isInPrimeiroPlano()) {
                Import.activites.getMainActivity().feedFragment.haveNewPostes(true);
            } else  {
                String titulo = getResources().getString(R.string.app_name);
                String texto = getResources().getString(R.string.novo_poste) + ": " + usuario.getDados().getNome();
                texto += "\n" + getResources().getString(R.string.esporte) + ": " + post.getEsporte();
                texto += "\n" + getResources().getString(R.string.mercado) + ": " + post.getMercado();
                texto += "\n" + getResources().getString(R.string.odd) + ": " + post.getOdd_minima() + " - " + post.getOdd_maxima();
                texto += "\n" + getResources().getString(R.string.horario) + ": " + post.getHorario_minimo() + " - " + post.getHorario_maximo();

                Intent intent = new Intent(getContext(), MainActivity.class);
                int channelId = Constantes.notification.id.NOVO_POST;
                Import.notificacao(getContext(), intent, channelId, titulo, texto);
            }
        } catch (Exception ignored) {}
    }

    /*private void CriarNotificacaoDaMensagem(String id_conversa, String titulo, String texto) {
        id_conversa = Criptografia.descriptografar(id_conversa);

        Intent intent;
        String[] tituloAux = titulo.split(",");
        String tituloReal;

        if(tituloAux.length == 1) {
            tituloReal = tituloAux[0];
            intent = new Intent(getApplicationContext(), ConversaActivity.class);
            intent.putExtra(Constantes.CONVERSA_CONTATO_ID, id_conversa);
            intent.putExtra(Constantes.CONVERSA_CONTATO_NOME, tituloReal);
        } else {
            tituloReal = "ProTips";
            intent = new Intent(getApplicationContext(), BatepapoActivity.class);
        }

        Import.Notificacao(getContext(), intent, tituloReal, texto);
    }*/

    //endregion

    private void getTipster(String id, boolean seguidorPendente) {
        Import.getFirebase.getReference()
                .child(Constantes.firebase.child.USUARIO)
                .child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User item = dataSnapshot.getValue(User.class);
                        if (item == null)
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

                        if (seguidorPendente) {
                            Import.get.solicitacao.add(item);
                            notificationPunterPendente(item.getDados());
                        } else {
                            notificationPunterAceito(item.getDados());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }

    private void getSeguidorPendente(String id) {
        User item = Import.get.tipsters.get(id);
        if (item == null) {
            getTipster(id, true);
        } else {
            notificationPunterPendente(item.getDados());
            Import.get.solicitacao.add(item);
        }
    }

    private void postAddChildEventList(String userId) {
        DatabaseReference ref =  Import.getFirebase.getReference()
                .child(Constantes.firebase.child.USUARIO)
                .child(userId)
                .child(Constantes.firebase.child.POSTES);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Post item = dataSnapshot.getValue(Post.class);
                if (item == null)
                    return;

                String id = item.getId();
                Import.get.seguindo.add(item);
                User user = Import.get.tipsters.get(item.getId_tipster());
                if (user != null) {
                    notificationNewPost(user, item);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Post item = dataSnapshot.getValue(Post.class);
                if (item == null)
                    return;

                Import.get.seguindo.remove(item);

                try {
                    Import.activites.getMainActivity().feedFragment.adapterUpdate();
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private boolean verifyApplicationRunning(@NonNull Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos;
        if (activityManager != null) {
            procInfos = activityManager.getRunningAppProcesses();
            for (int i = 0; i < procInfos.size(); i++) {
                if (procInfos.get(i).processName.equals("com.ookiisoftware.protips")) {
                    onDestroy();
                    return true;
                }
            }
        }
        return false;
    }

}
