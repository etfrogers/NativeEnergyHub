package com.example.energyhub.model

//import ninja.codingsolutions.solaredgeapiclient.SolarEdgeClientFactory
//import java.net.http.HttpClient
import com.etfrogers.ksolaredge.SolarEdgeClient

class SolarEdgeModel (
    val client: SolarEdgeClient = SolarEdgeClient()
)


/*
val factory = SolarEdgeClientFactory
.builder()
.apiKey("YOUR_API_KEY")
.apiUrl("https://monitoringapi.solaredge.com")
.httpClient(HttpClient.newHttpClient())
.build();

val client = factory.buildClient();

CompletableFuture<OverviewResponse> future = client.getOverviewResponse(YOUR_SITE_NUMBER)
.toCompletableFuture();

future.thenAcceptAsync(resp -> {
    System.out.println(String.format("Your solar panels are generating %s w/h of power",
        resp.getOverview().getCurrentPower().getPower()));
});
future.join();
*/
