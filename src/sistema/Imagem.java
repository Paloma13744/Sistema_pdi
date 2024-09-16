package sistema;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class Imagem extends JFrame {

    private JDesktopPane theDesktop;
    private BufferedImage imagemPessoa, imagemPaisagem;
    private JFileChooser fileChooser = new JFileChooser();
    private String pathPessoa = "", pathPaisagem = "";

    public Imagem() {
        super("PhotoIFMG");

        JMenuBar barraNavegacao = new JMenuBar();
        JMenu menuAbrirImagem = new JMenu("Abrir");
        JMenuItem menuAbrirImagemPessoa = new JMenuItem("Abrir a imagem da pessoa");
        JMenuItem menuAbrirImagemPaisagem = new JMenuItem("Abrir a imagem da paisagem");
        menuAbrirImagem.add(menuAbrirImagemPessoa);
        menuAbrirImagem.add(menuAbrirImagemPaisagem);

        barraNavegacao.add(menuAbrirImagem);
        JMenu menuProcessarImagem = new JMenu("Processar");
        JMenuItem menuCombinar = new JMenuItem("Combinar imagem da pessoa com a paisagem");

        menuProcessarImagem.add(menuCombinar);
        barraNavegacao.add(menuProcessarImagem);
        setJMenuBar(barraNavegacao);

        theDesktop = new JDesktopPane();
        getContentPane().add(theDesktop);

        menuAbrirImagemPessoa.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.CANCEL_OPTION) {
                    return;
                }
                pathPessoa = fileChooser.getSelectedFile().getAbsolutePath();
                try {
                    imagemPessoa = ImageIO.read(new File(pathPessoa));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                abrirImagem(pathPessoa, "Imagem da Pessoa");
            }
        });

        menuAbrirImagemPaisagem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.CANCEL_OPTION) {
                    return;
                }
                pathPaisagem = fileChooser.getSelectedFile().getAbsolutePath();
                try {
                    imagemPaisagem = ImageIO.read(new File(pathPaisagem));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                abrirImagem(pathPaisagem, "Imagem da Paisagem");
            }
        });

        menuCombinar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (imagemPessoa == null || imagemPaisagem == null) {
                    JOptionPane.showMessageDialog(null, "Por favor, abra as duas imagens primeiro.");
                    return;
                }
                inserirNoFundo(imagemPessoa, imagemPaisagem);
            }
        });

        setSize(800, 600);
        setVisible(true);
    }

    private void abrirImagem(String path, String titulo) {
        JInternalFrame frame = new JInternalFrame(titulo, true, true, true, true);
        Container container = frame.getContentPane();
        MyJPanel panel = new MyJPanel(path);
        container.add(panel, BorderLayout.CENTER);
        frame.pack();
        theDesktop.add(frame);
        frame.setVisible(true);
    }

    public void inserirNoFundo(BufferedImage imagemPessoa, BufferedImage imagemPaisagem) {
        int larguraPessoa = imagemPessoa.getWidth();
        int alturaPessoa = imagemPessoa.getHeight();
        int larguraPaisagem = imagemPaisagem.getWidth();
        int alturaPaisagem = imagemPaisagem.getHeight();

        BufferedImage resultadoNormal = new BufferedImage(larguraPaisagem, alturaPaisagem, BufferedImage.TYPE_INT_ARGB);
        Graphics gNormal = resultadoNormal.getGraphics();
        gNormal.drawImage(imagemPaisagem, 0, 0, null);
        gNormal.drawImage(removerFundoBranco(imagemPessoa), 0, 0, null);
        gNormal.dispose();

        exibirImagem("União imagens", resultadoNormal);

        BufferedImage imagemPaisagemSuavizada = aplicarFiltroMedia(imagemPaisagem, 5);
        BufferedImage resultadoSuavizado = new BufferedImage(larguraPaisagem, alturaPaisagem, BufferedImage.TYPE_INT_ARGB);
        Graphics gSuavizado = resultadoSuavizado.getGraphics();
        gSuavizado.drawImage(imagemPaisagemSuavizada, 0, 0, null);
        gSuavizado.drawImage(removerFundoBranco(imagemPessoa), 0, 0, null);
        gSuavizado.dispose();

        exibirImagem("União Fundo suavizado", resultadoSuavizado);
    }

    private BufferedImage removerFundoBranco(BufferedImage imagem) {
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        BufferedImage resultado = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);

        for (int i = 0; i < altura; i++) {
            for (int j = 0; j < largura; j++) {
                int pixel = imagem.getRGB(j, i);
                if (!isFundoBranco(pixel)) {
                    resultado.setRGB(j, i, pixel);
                }
            }
        }
        return resultado;
    }

    private boolean isFundoBranco(int pixel) {
        Color color = new Color(pixel, true);
        return (color.getRed() > 230 && color.getGreen() > 230 && color.getBlue() > 230);
    }

    private BufferedImage aplicarFiltroMedia(BufferedImage imagem, int tamanhoKernel) {
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        BufferedImage resultado = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);

        int[] kernel = new int[tamanhoKernel * tamanhoKernel];
        Arrays.fill(kernel, 1);
        int kernelSum = Arrays.stream(kernel).sum();

        for (int i = tamanhoKernel / 2; i < altura - tamanhoKernel / 2; i++) {
            for (int j = tamanhoKernel / 2; j < largura - tamanhoKernel / 2; j++) {
                int rSum = 0, gSum = 0, bSum = 0, aSum = 0;
                for (int ki = -tamanhoKernel / 2; ki <= tamanhoKernel / 2; ki++) {
                    for (int kj = -tamanhoKernel / 2; kj <= tamanhoKernel / 2; kj++) {
                        int pixel = imagem.getRGB(j + kj, i + ki);
                        Color color = new Color(pixel, true);
                        rSum += color.getRed();
                        gSum += color.getGreen();
                        bSum += color.getBlue();
                        aSum += color.getAlpha();
                    }
                }
                int r = rSum / kernelSum;
                int g = gSum / kernelSum;
                int b = bSum / kernelSum;
                int a = aSum / kernelSum;
                Color color = new Color(r, g, b, a);
                resultado.setRGB(j, i, color.getRGB());
            }
        }

        return resultado;
    }

    private void exibirImagem(String titulo, BufferedImage imagem) {
        JInternalFrame frame = new JInternalFrame(titulo, true, true, true, true);
        Container container = frame.getContentPane();
        JLabel label = new JLabel(new ImageIcon(imagem));
        container.add(label, BorderLayout.CENTER);
        frame.pack();
        theDesktop.add(frame);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        Imagem app = new Imagem();
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static class MyJPanel extends JPanel {
        private ImageIcon imageIcon;

        public MyJPanel(String caminhoImagem) {
            imageIcon = new ImageIcon(caminhoImagem);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            imageIcon.paintIcon(this, g, 0, 0);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(imageIcon.getIconWidth(), imageIcon.getIconHeight());
        }
    }

    private static class ProvedorImagens {
        public static void executarAposReceberImagens(int numeroDeImagens, Consumer<List<BufferedImage>> consumidor) {
            try {
                BufferedImage imagem1 = ImageIO.read(new File("caminho/para/imagem1.jpg"));
                BufferedImage imagem2 = ImageIO.read(new File("caminho/para/imagem2.jpg"));
                consumidor.accept(List.of(imagem1, imagem2));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
