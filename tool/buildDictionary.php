<?php
// Display reviews associated with business specified

  $dict = array();

  // filter associated reviews
  $fh = fopen('php://stdin', 'r');
  while (!feof($fh)) {
    $line = trim(fgets($fh));
    if (strlen($line) == 0)
      continue;
    $json = json_decode($line, true);
    $text = strtolower(preg_replace('/[^A-Za-z]/', ' ', $json["text"]));
    $arr = explode(' ', $text);
    foreach ($arr as $word) {
      $word = trim($word);
      if (strlen($word) == 0) {
        continue;
      }

      if (in_array($word, $dict)) {
        continue;
      }

      $dict[] = $word;
      echo $word."\n";
    }

  }
  fclose($fh);
