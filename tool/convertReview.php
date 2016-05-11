<?php
$fh = fopen('php://stdin', 'r');
while (!feof($fh)) {
  $line = trim(fgets($fh));
  if (strlen($line) == 0)
    continue;
  $json = json_decode($line, true);
  $review_id = $json["review_id"];
  $biz_id = $json["business_id"];
  $review = trim(str_replace(array("\t", "\n"), array(" ", " "), $json["text"]));
  echo $review_id."\t".$biz_id."\t".$review."\n";
}
fclose($fh);
