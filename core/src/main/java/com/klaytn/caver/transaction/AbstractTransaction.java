package com.klaytn.caver.transaction;

import com.klaytn.caver.Klay;
import com.klaytn.caver.account.AccountKeyRoleBased;
import com.klaytn.caver.crypto.KlaySignatureData;
import com.klaytn.caver.transaction.type.TransactionType;
import com.klaytn.caver.utils.Utils;
import com.klaytn.caver.wallet.keyring.Keyring;
import org.web3j.crypto.Hash;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

abstract public class AbstractTransaction {
    private Klay klaytnCall = null;
    int tag;
    String type;
    String from;
    String nonce = "";
    String gas;
    String gasPrice = "";
    String chainId = "";
    List<KlaySignatureData> signatures = new ArrayList<>();

    public AbstractTransaction(Klay klaytnCall, int tag, String type, String from, String nonce, String gas, String gasPrice, String chainId) {
        this.klaytnCall = klaytnCall;
        this.tag = tag;
        this.type = type;
        this.from = from;
        this.nonce = nonce;
        this.gas = gas;
        this.gasPrice = gasPrice;
        this.chainId = chainId;
    }

    public static class Builder<B extends AbstractTransaction.Builder> {
        final String type;
        final int tag;

        String from = "0x";
        String nonce = "";
        String gas;
        String gasPrice = "";
        String chainId = "";
        Klay klaytnCall = null;

        public Builder(String type, int tag) {
            this.type = type;
            this.tag = tag;
        }

        public B setFrom(String from) {
            this.from = from;
            return (B) this;
        }

        public B setNonce(String nonce) {
            this.nonce = nonce;
            return (B) this;
        }

        public B setGas(String gas) {
            this.gas = gas;
            return (B) this;
        }

        public B setGas(BigInteger gas) {
            setGas(Numeric.toHexStringWithPrefix(gas));
            return (B) this;
        }

        public B setGasPrice(String gasPrice) {
            this.gasPrice = gasPrice;
            return (B) this;
        }

        public B setGasPrice(BigInteger gasPrice) {
            setGasPrice(Numeric.toHexStringWithPrefix(gasPrice));
            return (B) this;
        }

        public B setChainId(String chainId) {
            this.chainId = chainId;
            return (B) this;
        }

        public B setChainId(BigInteger chainId) {
            setChainId(Numeric.toHexStringWithPrefix(chainId));
            return (B) this;
        }

        public B setKlaytnCall(Klay klaytnCall) {
            this.klaytnCall = klaytnCall;
            return (B) this;
        }
    }

    /**
     * Returns the RLP-encoded string of this transaction (i.e., rawTransaction).
     * @return String
     */
    abstract public String getRLPEncoding();

    /**
     * Returns the RLP-encoded string to make the signature of this transaction.
     * @return String
     */
    abstract String getCommonRLPEncodingForSignature();

    /**
     * Signs to the transaction with a single private key.
     * It sets index and Hasher default value.
     *   - index : 0
     *   - signer : TransactionHasher.getHashForSignature()
     * @param keyString The private key string.
     * @return AbstractTransaction
     * @throws IOException
     */
    public AbstractTransaction signWithKey(String keyString) throws IOException {
        Keyring keyring = Keyring.createFromPrivateKey(keyString);
        return this.signWithKey(keyring, 0, TransactionHasher::getHashForSignature);
    }

    /**
     * Signs to the transaction with a single private key.
     * It sets signer to TransactionHasher.getHashForSignature()
     * @param keyString The private key string.
     * @return AbstractTransaction
     * @throws IOException
     */
    public AbstractTransaction signWithKey(String keyString, int index) throws IOException {
        Keyring keyring = Keyring.createFromPrivateKey(keyString);
        return this.signWithKey(keyring, index, TransactionHasher::getHashForSignature);
    }


    /**
     * Signs to the transaction with a single private key.
     * It sets index 0.
     * @param keyString The private key string
     * @param signer The function to get hash of transaction.
     * @return AbstractTransaction
     * @throws IOException
     */
    public AbstractTransaction signWithKey(String keyString, Function<AbstractTransaction, String> signer) throws IOException {
        Keyring keyring = Keyring.createFromPrivateKey(keyString);
        return this.signWithKey(keyring, 0, signer);
    }

    /**
     * Signs to the transaction with a single private key.
     * @param keyString The private key string
     * @param index The index of private key to use in Keyring instance.
     * @param signer The function to get hash of transaction.
     * @return AbstractTransaction
     * @throws IOException
     */
    public AbstractTransaction signWithKey(String keyString, int index, Function<AbstractTransaction, String> signer) throws IOException {
        Keyring keyring = Keyring.createFromPrivateKey(keyString);
        return this.signWithKey(keyring, index, signer);
    }

    /**
     * Signs to the transaction with a single private key in the Keyring instance.
     * It sets index and Hasher default value.
     *   - index : 0
     *   - signer : TransactionHasher.getHashForSignature()
     * @param keyring The Keyring instance.
     * @return AbstractTransaction
     * @throws IOException
     */
    public AbstractTransaction signWithKey(Keyring keyring) throws IOException  {
        return this.signWithKey(keyring, 0, TransactionHasher::getHashForSignature);
    }

    /**
     * Signs to the transaction with a single private key in the Keyring instance.
     * It sets index 0.
     * @param keyring The Keyring instance.
     * @param signer The function to get hash of transaction.
     * @return AbstractTransaction
     * @throws IOException
     */
    public AbstractTransaction signWithKey(Keyring keyring, Function<AbstractTransaction, String> signer) throws IOException  {
        return this.signWithKey(keyring, 0, signer);
    }

    /**
     * Signs to the transaction with a single private key in the Keyring instance.
     * It sets signer to TransactionHasher.getHashForSignature()
     * @param keyring The Keyring instance.
     * @param index The index of private key to use in Keyring instance.
     * @return AbstractTransaction
     * @throws IOException
     */
    public AbstractTransaction signWithKey(Keyring keyring, int index) throws IOException {
        return this.signWithKey(keyring, index, TransactionHasher::getHashForSignature);
    }

    /**
     * Signs to the transaction with a single private key in the Keyring instance.
     * @param keyring The Keyring instance.
     * @param index The index of private key to use in Keyring instance.
     * @param signer The function to get hash of transaction.
     * @return AbstractTransaction
     * @throws IOException
     */
    public AbstractTransaction signWithKey(Keyring keyring, int index, Function<AbstractTransaction, String> signer) throws IOException {
        if(this.getType().equals(TransactionType.TxTypeLegacyTransaction.toString()) && keyring.isDecoupled()) {
            throw new IllegalArgumentException("A legacy transaction cannot be signed with a decoupled keyring.");
        }

        if(this.from.equals("0x")){
            this.from = keyring.getAddress();
        }

        if(!this.from.toLowerCase().equals(keyring.getAddress().toLowerCase())) {
            throw new IllegalArgumentException("The from address of the transaction is different with the address of the keyring to use");
        }

        this.fillTransaction();
        int role = this.type.startsWith("AccountUpdate") ? AccountKeyRoleBased.RoleGroup.ACCOUNT_UPDATE.getIndex() : AccountKeyRoleBased.RoleGroup.TRANSACTION.getIndex();

        String hash = signer.apply(this);
        KlaySignatureData sig = keyring.signWithKey(hash, Numeric.toBigInt(this.chainId).intValue(), role, index);

        this.appendSignatures(sig);

        return this;
    }

    /**
     * Signs to the transaction using all private keys in Keyring instance.
     * It sets signer to TransactionHasher.getHashForSignature()
     * @param keyString The private key string
     * @return AbstractTransaction
     * @throws IOException
     */
    public AbstractTransaction signWithKeys(String keyString) throws IOException {
        Keyring keyring = Keyring.createFromPrivateKey(keyString);

        return this.signWithKeys(keyring, TransactionHasher::getHashForSignature);
    }


    /**
     * Signs to the transaction using all private keys in Keyring instance.
     * @param keyString The private key string
     * @param signer The function to get hash of transaction.
     * @return AbstractTransaction
     * @throws IOException
     */
    public AbstractTransaction signWithKeys(String keyString, Function<AbstractTransaction, String> signer) throws IOException {
        Keyring keyring = Keyring.createFromPrivateKey(keyString);

        return this.signWithKeys(keyring, signer);
    }

    /**
     * Signs to the transaction using all private keys in Keyring instance.
     * It sets singer to TransactionHasher.getHashForSignature().
     * @param keyring The Keyring instance
     * @return AbstractTransaction
     * @throws IOException
     */
    public AbstractTransaction signWithKeys(Keyring keyring) throws IOException {
        return this.signWithKeys(keyring, TransactionHasher::getHashForSignature);
    }

    /**
     * Signs to the transaction using all private keys in Keyring
     * @param keyring The Keyring instance.
     * @param signer The function to get hash of transaction.
     * @return AbstractTransaction
     * @throws IOException
     */
    public AbstractTransaction signWithKeys(Keyring keyring, Function<AbstractTransaction, String> signer) throws IOException {
        if(this.getType().equals(TransactionType.TxTypeLegacyTransaction.toString()) && keyring.isDecoupled()) {
            throw new IllegalArgumentException("A legacy transaction cannot be signed with a decoupled keyring.");
        }

        if(this.from.equals("0x")){
            this.from = keyring.getAddress();
        }

        if(!this.from.toLowerCase().equals(keyring.getAddress().toLowerCase())) {
            throw new IllegalArgumentException("The from address of the transaction is different with the address of the keyring to use");
        }

        this.fillTransaction();
        int role = this.type.startsWith("AccountUpdate") ? AccountKeyRoleBased.RoleGroup.ACCOUNT_UPDATE.getIndex() : AccountKeyRoleBased.RoleGroup.TRANSACTION.getIndex();

        String hash = signer.apply(this);
        List<KlaySignatureData> sigList = keyring.signWithKeys(hash, Numeric.toBigInt(this.chainId).intValue(), role);

        this.appendSignatures(sigList);

        return this;
    }

    /**
     * Appends signatures to the transaction.
     * @param signatureData KlaySignatureData instance contains ECDSA signature data
     */
    public void appendSignatures(KlaySignatureData signatureData) {
        this.signatures.add(signatureData);
    }

    /**
     * Appends signatures to the transaction.
     * @param signatureData List of KlaySignatureData contains ECDSA signature data
     */
    public void appendSignatures(List<KlaySignatureData> signatureData) {
        this.signatures.addAll(signatureData);
    }

    /**
     * Combines signatures to the transaction from RLP-encoded transaction strings and returns a single transaction with all signatures combined.
     * When combining the signatures into a transaction instance,
     * an error is thrown if the decoded transaction contains different value except signatures.
     * @param rlpEncoded An array of RLP-encoded transaction strings.
     * @return String
     */
    public String combineSignatures(List<String> rlpEncoded) {
        boolean fillVariable = false;

        // If the signatures are empty, there may be an undefined member variable.
        // In this case, the empty information is filled with the decoded result.
        if(this.getSignatures().size() == 0) fillVariable = true;

        for(String encodedStr : rlpEncoded) {
            AbstractTransaction txObj = TransactionDecoder.decode(encodedStr);

            if(fillVariable) {
                if(this.getNonce().equals("")) this.nonce = txObj.getNonce();
                if(this.getGas().equals("")) this.nonce = txObj.getGas();
                fillVariable = false;
            }

            // Signatures can only be combined for the same transaction.
            // Therefore, compare whether the decoded transaction is the same as this.
            if(!this.checkTxField(txObj, false)) {
                throw new RuntimeException("Transactions containing different information cannot be combined.");
            }

            this.appendSignatures(txObj.getSignatures());
        }

        return this.getRLPEncoding();
    }

    /**
     * Returns a RawTransaction(RLP-encoded transaction string)
     * @return String
     */
    public String getRawTransaction() {
        return this.getRLPEncoding();
    }

    /**
     * Returns a hash string of transaction
     * @return String
     */
    public String getTransactionHash() {
        return Hash.sha3(this.getRLPEncoding());
    }

    /**
     * Returns a senderTxHash of transaction
     * @return String
     */
    public String getSenderTxHash() {
        return this.getTransactionHash();
    }

    /**
     * Returns an RLP-encoded transaction string for making signature.
     * @return String
     */
    public String getRLPEncodingForSignature() {
        validateOptionalValues();

        byte[] txRLP = Numeric.hexStringToByteArray(getCommonRLPEncodingForSignature());

        List<RlpType> rlpTypeList = new ArrayList<>();
        rlpTypeList.add(RlpString.create(txRLP));
        rlpTypeList.add(RlpString.create(chainId));
        rlpTypeList.add(RlpString.create(0));
        rlpTypeList.add(RlpString.create(0));
        byte[] encoded = RlpEncoder.encode(new RlpList(rlpTypeList));
        return Numeric.toHexString(encoded);
    }

    /**
     * Fills empty optional transaction field.(nonce, gasPrice, chainId)
     * @throws IOException
     */
    public void fillTransaction() throws IOException{
        if(klaytnCall != null) {
            this.nonce = klaytnCall.getTransactionCount(this.from, DefaultBlockParameterName.LATEST).send().getResult();
            this.chainId = klaytnCall.getChainID().send().getResult();
            this.gasPrice = klaytnCall.getGasPrice().send().getResult();
        }

        if(this.nonce.isEmpty() || this.chainId.isEmpty() || this.gasPrice.isEmpty()) {
            throw new RuntimeException("Cannot fill transaction data.(nonce, chainId, gasPrice");
        }
    }

    /**
     * Check equals txObj passed parameter and Current instance.
     * @param txObj The AbstractTransaction Object to compare
     * @param checkSig Check whether signatures field.
     * @return boolean
     */
    public boolean checkTxField(AbstractTransaction txObj, boolean checkSig) {
        if(this.getTag() != txObj.getTag()) return false;
        if(!this.getType().equals(txObj.getType())) return false;
        if(!this.getNonce().equals(txObj.getNonce())) return false;
        if(!this.getGas().equals(txObj.getGas())) return false;
        if(!this.getGasPrice().equals(txObj.getGasPrice())) return false;
        if(!this.getFrom().toLowerCase().equals(txObj.getFrom().toLowerCase())) return false;

        if(checkSig) {
            List<KlaySignatureData> dataList = this.getSignatures();
            if(dataList.size() != txObj.getSignatures().size()) return false;

            for(int i=0; i< dataList.size(); i++) {
                if(!dataList.get(i).equals(txObj.getSignatures().get(i))) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checks that member variables that can be defined by the user are defined.
     * If there is an undefined variable, an error occurs.
     */
    public void validateOptionalValues() {
        if(this.getNonce() == null || this.getNonce().isEmpty()) {
            throw new RuntimeException("nonce is undefined. Define nonce in transaction or use 'transaction.fillTransaction' to fill values.");
        }
        if(this.getGasPrice() == null || this.getGasPrice().isEmpty()) {
            throw new RuntimeException("gasPrice is undefined. Define gasPrice in transaction or use 'transaction.fillTransaction' to fill values.");
        }
        if(this.getChainId() == null || this.getChainId().isEmpty()) {
            throw new RuntimeException("chainId is undefined. Define chainId in transaction or use 'transaction.fillTransaction' to fill values.");
        }
    }

    public Klay getKlaytnCall() {
        return klaytnCall;
    }

    public void setKlaytnCall(Klay klaytnCall) {
        this.klaytnCall = klaytnCall;
    }

    public int getTag() {
        return tag;
    }

    public String getType() {
        return type;
    }

    public String getFrom() {
        return from;
    }

    public String getNonce() {
        return nonce;
    }

    public String getGas() {
        return gas;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public String getChainId() {
        return chainId;
    }

    public List<KlaySignatureData> getSignatures() {
        return signatures;
    }
}
