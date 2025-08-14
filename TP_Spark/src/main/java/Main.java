import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("Ventes").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> rddLines = sc.textFile("/app/ventes.txt");
        long totalVentes = rddLines.count();
        System.out.println("Total Ventes : " + totalVentes);
        JavaRDD<String[]> rddOrders = rddLines.map(line -> line.split(" "));
        JavaPairRDD<String, Integer> rddPrixVille = rddOrders.mapToPair(order -> {
            return new Tuple2<>(order[1], Integer.parseInt(order[3]));
        });
        JavaPairRDD<String,Integer> totalParVille = rddPrixVille.reduceByKey(Integer::sum);
        totalParVille.foreach(entry -> {
            System.out.println("Ville: " + entry._1 + " - Total: " + entry._2);
        });
        JavaPairRDD<String,Integer> rddPrixAnnee = rddOrders.mapToPair(order -> {
            String ville = order[1];
            String annee = order[0].split("/")[2];
            int prix = Integer.parseInt(order[3]);
            return new Tuple2<>(ville + "-" + annee, prix);
        });
        JavaPairRDD<String,Integer> totalParAnneeVille = rddPrixAnnee.reduceByKey(Integer::sum);
        JavaPairRDD<String,Integer> sorted = totalParAnneeVille.sortByKey();
        sorted.foreach(entry -> {
            String[] villeAnne = entry._1.split("-");
            String ville = villeAnne[0];
            String annee = villeAnne[1];
            System.out.println("ville " + ville + " - Annee: " + annee + " - Total: " + entry._2);
        });
        sc.close();
    }
}
