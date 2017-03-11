# Word2vec
The program of word2vec for java, contains POS-CBOW and POS-Skip-gram
* POS-CBOW
  The continue bag of word laugage model with part-of-speech.
# Usage

            Configuration conf=new Configuration();
            conf.setCBOW(0);
            conf.setThreads(2);
            conf.setMiniCount(0);
            Word2vec vectest=new Word2vec("text8","./cbow.bin",conf);
            vectest.TrainTask(vectest);

# License
  Apache Lisence 2.0
