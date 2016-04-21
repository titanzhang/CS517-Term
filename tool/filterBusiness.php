<?php
if ($argc < 2) {
  // List all possible categories
  $fh = fopen('php://stdin', 'r');
  while (!feof($fh)) {
    $line = fgets($fh);
    if (trim($line) == "") continue;
    $json = json_decode($line, true);
    foreach ($json["categories"] as $category) {
      echo $category."\n";
    }
  }
  fclose($fh);
} else {
  // Display businesses in the categories provided by command line parameter
  $categoryList = explode(",", $argv[1]);
  $fh = fopen('php://stdin', 'r');
  while (!feof($fh)) {
    $line = fgets($fh);
    if (trim($line) == "")
      continue;
    $json = json_decode($line, true);
    $inCategory = false;
    foreach ($json["categories"] as $category) { 
      if (in_array($category, $categoryList)) {
        $inCategory = true;
        break;
      }
    }

    if ($inCategory)
      echo $line;
  }
  fclose($fh);
}
