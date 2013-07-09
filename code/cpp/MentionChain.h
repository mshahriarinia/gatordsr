
#ifndef MENTION_CHAIN_H
#define MENTION_CHAIN_H

#include <fstream>
#include <string>

#include "streamcorpus_types.h"
#include "streamcorpus_constants.h"


class MentionChain {

  constexpr static const char *media_sdd = "/media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/"
    "kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/%s/%s";
  constexpr static const char *media_sde = "/media/sde/s3.amazonaws.com/aws-publicdatasets/trec/kba/"
    "kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/%s/%s";
  constexpr static const char *tmp_prefix = "/var/tmp";
  constexpr static const char *gpgDecompress = "gpg --quiet --no-verbose --no-permission-warning --trust-model always --output - --decrypt %s | xz --decompress > %s";

private:
  std::string day;
  std::string fileName;
  int stream_index;
  std::string docid;
  std::string entity;
  streamcorpus::StreamItem si;

  // TODO map or pointer to mentions
  // TODO - Keep a pool of docs??

public:
  MentionChain() {}
  MentionChain(streamcorpus::StreamItem _si, std::string _entity): si(_si), entity(_entity) { }

  /**
    * TODO: This function searches for all the entity mentions in the streamitem documents.
    * TODO: This also extracts/creates a feature vector
    */
  void init ();

  /** Take a line from a runXLog.txt file and make it into a MentionChain */
  static std::vector<MentionChain> ReadLine(std::string line);

  /** Take a thrift file and extract all the stream items and put them in a vector */
  static std::vector<streamcorpus::StreamItem> FileToStreamItem(std::string filePath); 

  /** Check to see if a file exists */
  static bool fexist (const char *fileName) { return std::ifstream (fileName); }

  /**
    * Decompresses and dencrypts a file to a temporary file and returns the file name.
    */
  static std::string CreateTempGPGFile (std::string fileName);
};




#endif  // MENTION_CHAIN_H

