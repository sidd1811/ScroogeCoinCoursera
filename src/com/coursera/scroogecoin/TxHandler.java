package com.coursera.scroogecoin;

import java.util.ArrayList;

public class TxHandler {

	//	public ArrayList<UTXOPool> pubLedger = null;
	//	
	//	public ArrayList<UTXOPool> pub

	private UTXOPool utxoPool;

	/**
	 * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
	 * {@code utxoPool}. This should make a defensive copy of utxoPool by using the UTXOPool(UTXOPool uPool)
	 * constructor.
	 */
	public TxHandler(UTXOPool utxoPool) {
		// IMPLEMENT THIS
		this.utxoPool = utxoPool;
	}


	public boolean isValidTx(Transaction tx) {
		// IMPLEMENT THIS


		if(this.utxoPool != null && tx != null) {


			double inputValue = 0, outputValue = 0;

			//Outputs belonging to input transactions hashes
			ArrayList<Transaction.Output> arrInputTx = new ArrayList<Transaction.Output>();

			//Outputs belonging to given transactions outputs
			ArrayList<Transaction.Output> arrOutputTx = tx.getOutputs();

			UTXO utxo = null;

			for(Transaction.Input input : tx.getInputs()) {

				utxo = new UTXO(input.prevTxHash, input.outputIndex);

				if(utxoPool.contains(utxo)) {
					Transaction.Output output = utxoPool.getTxOutput(utxo);
					if(output != null) {
						if(Crypto.verifySignature(output.address, tx.getRawDataToSign(tx.getInputs().indexOf(input)), input.signature)) {
							if(!arrInputTx.contains(output)) {
								arrInputTx.add(output);
								inputValue += output.value;
							}
							else
								return false;
						}else 
							return false;
					}else 
						return false;
				}else 
					return false;
			}


			for(Transaction.Output output : arrOutputTx) {
				if(output.value <0)
					return false;
				outputValue += output.value;
			}

			if(outputValue > inputValue) {
				return false;
			}

			return true;

		}
		return false;
	}

	/**
	 * Handles each epoch by receiving an unordered array of proposed transactions, checking each
	 * transaction for correctness, returning a mutually valid array of accepted transactions, and
	 * updating the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
		// IMPLEMENT THIS

		if(possibleTxs != null) {
			ArrayList<Transaction> validTxsList = new ArrayList<Transaction>();
			ArrayList<Transaction> possibleTxsList = new ArrayList<Transaction>();

			for(Transaction tx : possibleTxs) {
				possibleTxsList.add(tx);
			}

			for(int i = 0; i<possibleTxsList.size(); i++) {
				if(isValidTx(possibleTxsList.get(i))) {
					//updatePool
					updatePool(possibleTxsList.get(i));

					//Add possible transaction to list of valid transactions
					validTxsList.add(possibleTxsList.get(i));

					//Update possible transactions list 
					possibleTxsList.remove(i);
					if(possibleTxsList.size()>0)
						i = -1;
				}
			}
			validTxsList.toArray(possibleTxs);
			return possibleTxs;
		}
		return null;
	}

	public void updatePool(Transaction tx) {

		UTXO utxo = null;

		if(tx!= null) {
			for(Transaction.Input input : tx.getInputs()) {
				utxo = new UTXO(input.prevTxHash, input.outputIndex);
				this.utxoPool.removeUTXO(utxo);
				tx.addSignature(input.signature, tx.getInputs().indexOf(input));

				utxo = new UTXO(tx.getHash(), tx.getInputs().indexOf(input));
				this.utxoPool.addUTXO(utxo, tx.getOutput(tx.getInputs().indexOf(input)));
				
			}
			tx.finalize();
		}else
			return;
	}
}
