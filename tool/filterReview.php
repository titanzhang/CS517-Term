<?php
// Display reviews associated with business specified

if ($argc >1 ) {
  // Parse business file to get all business ids
  $businessFile = $argv[1];
  $businessIds = array();
  $fh = fopen($businessFile, 'r');
  while (!feof($fh)) {
    $line = trim(fgets($fh));
    if (strlen($line) == 0)
      continue;
    $json = json_decode($line, true);
    $businessIds[] = $json["business_id"];
  }
  fclose($fh);

  // filter associated reviews
  $fh = fopen('php://stdin', 'r');
  while (!feof($fh)) {
    $line = trim(fgets($fh));
    if (strlen($line) == 0)
      continue;
    $json = json_decode($line, true);
    if (in_array($json["business_id"], $businessIds))
      echo $line."\n";

  }
  fclose($fh);
}
