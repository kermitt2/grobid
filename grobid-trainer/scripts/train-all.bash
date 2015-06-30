# Trains all models
if [ $# -lt 3 ]
then
  echo "$0 grobid-trainer-one-jar.jar grobid-home-path email-handle-when-done"
  echo "Will train all models and send email per 3rd argument when done"
  exit 1
fi

for model in "affiliation" "chemical" "date" "citation" "ebook" "fulltext" "header" "name-citation" "name-header" "patent" "segmentation" "reference-segmenter"
do
    echo "java -server -mx15g -jar $1 0 $model -gH $2"
done
sendmail -s "Grobid Training Complete" $3 < /dev/null
