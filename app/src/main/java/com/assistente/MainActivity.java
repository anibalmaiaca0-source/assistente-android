package com.assistente;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int VOICE_REQUEST = 100;
    private static final int AUDIO_PERMISSION = 101;
    private static final int CALL_PERMISSION = 102;

    private TextView status;
    private TextToSpeech voz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        criarInterface();

        voz = new TextToSpeech(this, resultado -> {
            if (resultado == TextToSpeech.SUCCESS) {
                int idioma = voz.setLanguage(new Locale("pt", "AO"));

                if (idioma == TextToSpeech.LANG_MISSING_DATA ||
                    idioma == TextToSpeech.LANG_NOT_SUPPORTED) {

                    voz.setLanguage(new Locale("pt", "BR"));
                }
            }
        });

        solicitarMicrofone();
    }

    private void criarInterface() {

        LinearLayout layout = new LinearLayout(this);

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 60, 40, 40);

        TextView titulo = new TextView(this);
        titulo.setText("🤖 ASSISTENTE");
        titulo.setTextSize(30);

        TextView descricao = new TextView(this);
        descricao.setText(
                "\nDiga um comando como:\n" +
                "• Abrir WhatsApp\n" +
                "• Abrir YouTube\n" +
                "• Abrir navegador\n" +
                "• Abrir configurações\n" +
                "• Ir para tela inicial\n" +
                "• Ligar para 923...\n"
        );
        descricao.setTextSize(17);

        status = new TextView(this);
        status.setText("\n🟢 Pronto para receber comandos.");
        status.setTextSize(18);

        Button falar = new Button(this);
        falar.setText("🎙️ FALAR");

        falar.setOnClickListener(v -> ouvir());

        layout.addView(titulo);
        layout.addView(descricao);
        layout.addView(status);
        layout.addView(falar);

        setContentView(layout);
    }

    private void solicitarMicrofone() {

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    AUDIO_PERMISSION
            );
        }
    }

    private void ouvir() {

        Intent intent =
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "pt-BR"
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Fale um comando..."
        );

        try {

            status.setText("\n🎙️ Estou ouvindo...");

            startActivityForResult(
                    intent,
                    VOICE_REQUEST
            );

        } catch (Exception e) {

            status.setText(
                    "\n❌ Reconhecimento de voz indisponível."
            );

            falar(
                    "Não consegui acessar o reconhecimento de voz."
            );
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode != VOICE_REQUEST ||
                resultCode != RESULT_OK ||
                data == null) {

            status.setText(
                    "\n🟡 Não consegui entender o comando."
            );

            return;
        }

        ArrayList<String> resultados =
                data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS
                );

        if (resultados == null ||
                resultados.isEmpty()) {

            status.setText(
                    "\n🟡 Não ouvi nenhum comando."
            );

            return;
        }

        String comando =
                resultados.get(0)
                        .toLowerCase(Locale.ROOT)
                        .trim();

        status.setText(
                "\n🗣️ Você disse:\n" + comando
        );

        executarComando(comando);
    }

    private void executarComando(String comando) {

        if (comando.contains("abrir whatsapp")) {

            abrirAplicativo(
                    "com.whatsapp",
                    "WhatsApp"
            );

            return;
        }

        if (comando.contains("abrir youtube")) {

            abrirAplicativo(
                    "com.google.android.youtube",
                    "YouTube"
            );

            return;
        }

        if (comando.contains("abrir navegador") ||
                comando.contains("abrir browser") ||
                comando.contains("abrir google")) {

            abrirNavegador();

            return;
        }

        if (comando.contains("abrir configurações") ||
                comando.contains("abrir configuracoes") ||
                comando.contains("abrir definições") ||
                comando.contains("abrir definicoes")) {

            abrirConfiguracoes();

            return;
        }

        if (comando.contains("ir para tela inicial") ||
                comando.contains("ir para o início") ||
                comando.contains("ir para o inicio") ||
                comando.equals("início") ||
                comando.equals("inicio")) {

            irParaInicio();

            return;
        }

        if (comando.startsWith("ligar para ")) {

            String numero =
                    comando.substring("ligar para ".length())
                            .trim();

            ligar(numero);

            return;
        }

        if (comando.contains("olá") ||
                comando.contains("ola") ||
                comando.contains("oi")) {

            falar(
                    "Olá! Estou pronto. " +
                    "Diga o que você precisa."
            );

            return;
        }

        if (comando.contains("quem é você") ||
                comando.contains("quem e voce")) {

            falar(
                    "Eu sou o seu Assistente Android."
            );

            return;
        }

        falar(
                "Ainda não aprendi esse comando."
        );
    }

    private void abrirAplicativo(
            String pacote,
            String nome) {

        try {

            Intent intent =
                    getPackageManager()
                            .getLaunchIntentForPackage(
                                    pacote
                            );

            if (intent != null) {

                startActivity(intent);

                falar(
                        "Abrindo o " + nome + "."
                );

            } else {

                falar(
                        "Não encontrei o " + nome +
                        " instalado."
                );
            }

        } catch (Exception e) {

            falar(
                    "Não consegui abrir o " + nome + "."
            );
        }
    }

    private void abrirNavegador() {

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com")
                    );

            startActivity(intent);

            falar("Abrindo o navegador.");

        } catch (Exception e) {

            falar(
                    "Não consegui abrir o navegador."
            );
        }
    }

    private void abrirConfiguracoes() {

        try {

            Intent intent =
                    new Intent(
                            Settings.ACTION_SETTINGS
                    );

            startActivity(intent);

            falar(
                    "Abrindo as configurações."
            );

        } catch (Exception e) {

            falar(
                    "Não consegui abrir as configurações."
            );
        }
    }

    private void irParaInicio() {

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_MAIN
                    );

            intent.addCategory(
                    Intent.CATEGORY_HOME
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(intent);

        } catch (Exception e) {

            falar(
                    "Não consegui voltar para a tela inicial."
            );
        }
    }

    private void ligar(String numero) {

        numero =
                numero.replaceAll(
                        "[^0-9+]",
                        ""
                );

        if (numero.isEmpty()) {

            falar(
                    "Não consegui identificar o número."
            );

            return;
        }

        if (checkSelfPermission(
                Manifest.permission.CALL_PHONE
        ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.CALL_PHONE
                    },
                    CALL_PERMISSION
            );

            falar(
                    "Preciso da permissão para fazer chamadas."
            );

            return;
        }

        try {

            Intent chamada =
                    new Intent(
                            Intent.ACTION_CALL
                    );

            chamada.setData(
                    Uri.parse(
                            "tel:" + numero
                    )
            );

            startActivity(chamada);

        } catch (Exception e) {

            falar(
                    "Não consegui iniciar a chamada."
            );
        }
    }

    private void falar(String texto) {

        if (voz != null) {

            voz.speak(
                    texto,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "assistente"
            );
        }
    }

    @Override
    protected void onDestroy() {

        if (voz != null) {

            voz.stop();
            voz.shutdown();
        }

        super.onDestroy();
    }
}
