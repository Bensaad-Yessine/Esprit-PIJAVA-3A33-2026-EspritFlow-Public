package piJava.mains;

import piJava.api.StatsApiServer;

public final class ApiLauncher {

    private ApiLauncher() {
    }

    public static void main(String[] args) throws Exception {
        StatsApiServer.getInstance().start();
        System.out.println("API ready on http://localhost:" + StatsApiServer.getInstance().getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(StatsApiServer.getInstance()::stop));
        Thread.currentThread().join();
    }
}

