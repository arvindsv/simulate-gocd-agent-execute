import java.io.*;
import java.util.Date;

public class SimulateAgent {
    public static void main(String[] args) throws Exception {
        new SimulateAgent().runProcess(args);
    }

    private void runProcess(String[] args) throws IOException {
        Process process = new ProcessBuilder(args).start();
        StreamPumper outputStream = StreamPumper.pump(process.getInputStream(), "stdout");
        StreamPumper errorStream = StreamPumper.pump(process.getErrorStream(), "stderr");

        try {
            process.waitFor();
            outputStream.readToEnd();
            errorStream.readToEnd();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            close(process);
        }
    }

    private void close(Process process) {
        try {
            closeQuietly(process.getInputStream());
            closeQuietly(process.getOutputStream());
            closeQuietly(process.getErrorStream());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private void closeQuietly(Closeable stream) {
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class StreamPumper implements Runnable {
        private BufferedReader stream;
        private boolean completed = false;

        public StreamPumper(InputStream stream) {
            this.stream = new LineNumberReader(new InputStreamReader(stream));
        }

        public static StreamPumper pump(InputStream stream, String name) {
            StreamPumper pumper = new StreamPumper(stream);
            new Thread(pumper, name).start();
            return pumper;
        }

        @Override
        public void run() {
            try {
                String s;
                s = stream.readLine();
                while (s != null) {
                    consumeLine(s);
                    s = stream.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeQuietly();
                completed = true;
            }
        }

        private void closeQuietly() {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void consumeLine(String line) {
            System.out.println(line);
        }

        public void readToEnd() {
            while (!completed) {
                try {
                    Thread.sleep(1000);
                    System.out.println(Thread.currentThread().getName() + " Waiting at " + new Date() + " ...");
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
